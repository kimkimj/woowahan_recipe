package com.woowahan.recipe.controller.ui;

import com.woowahan.recipe.domain.dto.itemDto.ItemCreateReqDto;
import com.woowahan.recipe.domain.dto.itemDto.ItemUpdateReqDto;
import com.woowahan.recipe.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    /**
     * 재료 등록(관리자, 판매자)
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("itemCreateReqDto", new ItemCreateReqDto());
        return "item/createForm";
    }

    @PostMapping("/create")
    //동작 test용
//    public String create(@Valid ItemCreateReqDto reqDto, BindingResult bindingResult, Model model, Authentication authentication) {
    public String create(@Valid @ModelAttribute ItemCreateReqDto reqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.info("bindingResult = {}", bindingResult);
            return "item/createForm";
        }
        itemService.createItem(reqDto, "ididid");
        return "redirect:/";
    }

    /**
     * 재료 수정(관리자, 판매자)
     */
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id,  Model model) {
        model.addAttribute("id", id);
        model.addAttribute("itemUpdateReqDto", new ItemUpdateReqDto());
        return "item/updateForm";
    }

    @PostMapping("/update/{id}")
    //동작 test용
//    public String update(@PathVariable Long id, @Valid @ModelAttribute ItemUpdateReqDto reqDto, BindingResult bindingResult, Model model, Authentication authentication) {
    public String update(@Valid @ModelAttribute ItemUpdateReqDto reqDto, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult = {}", bindingResult);
            return "item/updateForm";
        }
        itemService.updateItem(id, reqDto, "ididid");
        return "redirect:/";
    }

}
