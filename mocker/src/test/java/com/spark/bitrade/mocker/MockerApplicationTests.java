package com.spark.bitrade.mocker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MockerApplicationTests {

	@Test
	public void contextLoads() throws Exception {
		System.out.println("----------ok---------begin---------------");
		//OkHttpClient client = new OkHttpClient();
		//科学上网 代理测试
		OkHttpClient client = new OkHttpClient.Builder()
				.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)))
				//.addInterceptor(interceptor)
				.build();

		Request request = new Request.Builder()
				.url("https://api.huobi.pro/market/trade?AccessKeyId=44dfbd12-89145178-dffabcb2-42ebe&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2018-04-15T04%3A54%3A34&symbol=btcusdt")
				//.header("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36")
				//.url("https://www.google.com.hk/?gws_rd=ssl")
				.addHeader("Accept", "text/html")
				.build();

		Response response = client.newCall(request).execute();
		System.out.println(response);
		if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

		System.out.println(response.body().string());

		System.out.println("----------ok---------end---------------");
	}

	@Test
	public void contextLoads2() throws Exception {
		OkHttpClient client = new OkHttpClient();
		System.out.println("----------ok---------begin---------------");
		//正常代理
		Request request = new Request.Builder()
				.url("https://github.com")
				.header("User-Agent", "My super agent")
				.addHeader("Accept", "text/html")
				.build();

		/*Request request = new Request.Builder()
				.header("Authorization", "Client-ID " )
				.url("https://api.imgur.com/3/image")
				.post(requestBody)
				.build();*/

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

		System.out.println(response.body().string());

		System.out.println("----------ok---------end---------------");
	}

}
