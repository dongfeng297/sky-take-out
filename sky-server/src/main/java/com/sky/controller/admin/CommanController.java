package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommanController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String extend = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

        try {
            String url = aliOssUtil.upload(file.getBytes(), UUID.randomUUID() + extend);
            return Result.success(url);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }



    }
}
