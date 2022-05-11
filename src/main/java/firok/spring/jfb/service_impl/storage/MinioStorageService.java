package firok.spring.jfb.service_impl.storage;

import firok.spring.jfb.service.ExceptionIntegrative;
import firok.spring.jfb.service.storage.IStorageIntegrative;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 基于 MinIO 的数据持久化实现
 */
@ConditionalOnExpression("${app.service-storage.minio.enable}")
@Service
public class MinioStorageService implements IStorageIntegrative
{
	@Value("${app.service-storage.minio.url}")
	public String url;

	@Value("${app.service-storage.minio.username}")
	public String username;

	@Value("${app.service-storage.minio.password}")
	public String password;

	public MinioClient client;

	@PostConstruct
	protected void connectMinio()
	{
		client = MinioClient.builder()
				.endpoint(url)
				.credentials(username, password)
				.build();
	}

	@Override
	public void store(String nameBucket, String nameObject, InputStream is) throws ExceptionIntegrative
	{
		var args = PutObjectArgs.builder()
				.bucket(nameBucket)
				.object(nameObject)
				.stream(is, -1, -1) // todo 这里可能需要开放配置出去
				.contentType(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)
				.build();
		try
		{
			client.putObject(args);
		}
		catch (Exception e)
		{
			throw new ExceptionIntegrative(e);
		}
	}

	@Override
	public void extract(String nameBucket, String nameObject, OutputStream os) throws ExceptionIntegrative
	{
		var args = GetObjectArgs.builder()
				.bucket(nameBucket)
				.object(nameObject)
				.build();
		try
		{
			var ret = client.getObject(args);
			ret.transferTo(os);
		}
		catch (Exception e)
		{
			throw new ExceptionIntegrative(e);
		}
	}
}