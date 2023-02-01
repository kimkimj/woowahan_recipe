package com.woowahan.recipe.controller.ui;

import com.woowahan.recipe.domain.dto.reviewDto.ReviewCreateRequest;
import com.woowahan.recipe.domain.dto.reviewDto.ReviewListResponse;
import com.woowahan.recipe.domain.dto.recipeDto.RecipeFindResDto;
import com.woowahan.recipe.domain.dto.userDto.UserJoinReqDto;
import com.woowahan.recipe.domain.dto.userDto.UserLoginReqDto;
import com.woowahan.recipe.domain.dto.userDto.UserResponse;
import com.woowahan.recipe.domain.dto.userDto.*;
import com.woowahan.recipe.domain.entity.UserEntity;
import com.woowahan.recipe.service.FindService;
import com.woowahan.recipe.service.ReviewService;
import com.woowahan.recipe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final FindService findService;

    // 관리자 페이지
    @GetMapping("/admin/users")
    public String admin(Model model, @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponse> userList = userService.findAll(pageable);
        model.addAttribute("userList", userList);
        return "user/admin";
    }

    // 회원가입
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("userJoinReqDto", new UserJoinReqDto());
        return "user/joinForm";
    }

    @PostMapping("/join")
    public String join(@Valid UserJoinReqDto form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "user/joinForm";
        }
        model.addAttribute("userJoinReqDto", new UserJoinReqDto());
        userService.join(form);
        return "redirect:/login";
    }

    // 로그인
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("userLoginReqDto", new UserLoginReqDto());
        return "user/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute UserLoginReqDto userLoginReqDto, BindingResult result,
                        HttpServletRequest httpServletRequest){
        if (result.hasErrors()) {
            result.getFieldErrors().stream().forEach(err ->
                    log.info("field={} value={} msg={}", err.getField(), err.getRejectedValue(), err.getDefaultMessage()));
            return "user/loginForm";
        }

        // 세션 넣기
        httpServletRequest.getSession().invalidate();
        HttpSession session = httpServletRequest.getSession(true);

        String token = userService.login(userLoginReqDto.getUserName(), userLoginReqDto.getPassword());
        session.setAttribute("jwt", "Bearer " + token);
        String checkJwt = (String) session.getAttribute("jwt");
        log.info("checkJwt={}", checkJwt);
        log.info("token={}", token);
        session.setMaxInactiveInterval(1800);

        return "redirect:/";
    }

    @GetMapping("/users/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("jwt");
        session.invalidate();

        return "redirect:/";
    }

    // 마이페이지
    @GetMapping("/users/my")
    public String myPage(Model model, Authentication authentication) {
        UserEntity user = findService.findUserName(authentication.getName());
        UserResponse userResponse = UserResponse.toUserResponse(user);
        model.addAttribute("user", userResponse);
        return "user/my/myInfo";
    }

    // 회원 정보 수정
    @GetMapping("/users/my/update")
    public String updateForm(Model model, Authentication authentication) {
        log.info("user22={}", authentication.getName());
        UserEntity user = findService.findUserName(authentication.getName());
        model.addAttribute("user", user);
        // 로그인이 되어있는 유저의 id와 수정페이지에 접속하는 id가 같아야 함
        return "user/updateForm";
    }

    @PostMapping("/users/my/update")
    public String updateUser(Model model, UserUpdateReqDto reqDto, Authentication authentication) {
        UserResponse userResponse = userService.updateInfo(reqDto, authentication.getName());
        model.addAttribute("user", userResponse);

        return "redirect:/users/my";
    }

    @GetMapping("/users/my/update/password")
    public String updatePasswordForm(Model model) {
        model.addAttribute("userPasswordReqDto", new UserPasswordReqDto());
        return "user/my/passwordForm";
    }

    @PostMapping("/users/my/update/password")
    public String updatePassword(Model model, UserPasswordReqDto userPasswordReqDto, Authentication authentication) {
        model.addAttribute("userPasswordReqDto", userPasswordReqDto);
        userService.updatePassword(userPasswordReqDto.getPassword(), authentication.getName());
        return "redirect:/users/my";
    }

    @GetMapping("/my/get-reviews")
    public String myGetReviews() {
        return "user/my/myGetReview";
    }

    // 내가 작성한 리뷰 목록
    @GetMapping("/my/reviews")
    public String myReviews(Model model, Authentication authentication,
                            @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = authentication.getName();
        Page<ReviewListResponse> reviewList = reviewService.findAllReviewsByUser(username, pageable);

        int nowPage = reviewList.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, reviewList.getTotalPages());
        int lastPage = reviewList.getTotalPages();

        model.addAttribute("allMyReviews", reviewList);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("lastPage", lastPage);

        return "user/my/myReviews";
    }

    // 리뷰 수정
    @GetMapping("/update/{recipeId}/{reviewId}")
    public String updateReview(@PathVariable Long recipeId, @PathVariable Long reviewId, Model model) {
        model.addAttribute("previousComment", reviewService.findReviewById(reviewId).getReviewComment());
        model.addAttribute("reviewUpdateRequest", new ReviewCreateRequest());
        model.addAttribute("recipeId", recipeId);
        model.addAttribute("reviewId", reviewId);
        return "review/updateForm";
    }

    @PostMapping("/update/{recipeId}/{reviewId}")
    public String update(@Valid @ModelAttribute ReviewCreateRequest request, BindingResult bindingResult,
                         @PathVariable Long recipeId, @PathVariable Long reviewId,
                        Authentication authentication, Model model) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult = {}", bindingResult);
            return "review/updateForm";
        }
        String username = authentication.getName();
        reviewService.updateReview(recipeId, reviewId, request, username);
        return "redirect:/my/reviews";
    }

    // 리뷰 삭제
    @GetMapping("/delete/{recipeId}/{reviewId}")
    public String delete(@PathVariable Long recipeId, @PathVariable Long reviewId, Authentication authentication) {
        String username = authentication.getName();
        reviewService.deleteReview(recipeId, reviewId, username);
        return "redirect:/my/reviews";
    }

    @GetMapping("/users/my/recipe-like")
    public String myLikeRecipe(Model model, Authentication authentication, @PageableDefault(size = 20)Pageable pageable) {
        Page<RecipeFindResDto> myLikeRecipeList = findService.findMyLikeRecipe(authentication.getName(), pageable);
        model.addAttribute("myLikeRecipeList", myLikeRecipeList);
        return "user/my/myLikeRecipe";
    }
}
