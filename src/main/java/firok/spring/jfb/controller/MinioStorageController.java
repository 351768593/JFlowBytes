package firok.spring.jfb.controller;

import firok.spring.jfb.service_impl.storage.MinioStorageIntegrative;
import io.minio.GetObjectArgs;
import io.minio.errors.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Api(description = "MinIO 持久化储存相关接口")
@ConditionalOnBean(MinioStorageIntegrative.class)
@RestController
@RequestMapping("/api/storage/minio")
public class MinioStorageController
{
	@Autowired
	MinioStorageIntegrative service;

	@ApiOperation("获取指定文件数据, 返回文件二进制数据流")
	@GetMapping("/read/{nameBucket}/{nameFile}")
	public void read(
			@ApiParam("桶名称") @PathVariable("nameFile") String nameFile,
			@ApiParam("文件名称") @PathVariable("nameBucket") String nameBucket,
			HttpServletResponse response
	) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException
	{
		response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(nameFile, StandardCharsets.UTF_8));

		var args = GetObjectArgs.builder()
				.bucket(nameBucket)
				.object(nameFile)
				.build();
		var obj = service.client.getObject(args);

		try(var os = response.getOutputStream())
		{
			obj.transferTo(os);
		}
	}
}
