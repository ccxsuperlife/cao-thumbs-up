package com.cao.thumbsup.controller;

import com.cao.thumbsup.common.BaseResponse;
import com.cao.thumbsup.common.ResultUtils;
import com.cao.thumbsup.model.dto.thumb.DoThumbRequest;
import com.cao.thumbsup.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        boolean result = thumbService.doThumb(doThumbRequest, request);
        return ResultUtils.success(result);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        boolean result = thumbService.undoThumb(doThumbRequest, request);
        return ResultUtils.success(result);
    }
}
