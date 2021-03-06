package com.ecsteam.firehose.nozzle.annotation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ecsteam.firehose.nozzle.FirehoseProperties;
import com.ecsteam.firehose.nozzle.FirehoseReader;
import com.ecsteam.firehose.nozzle.annotation.application.ApplicationTest1;
import com.ecsteam.firehose.nozzle.configuration.FirehoseNozzlePropertiesConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ApplicationTest1.class})
public class EnableFirehoseNozzleDefaultConfigTest {

	@Autowired
	ApplicationContext context;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testDefaultAnnotation() {
		
		assertNotNull(context);
		FirehoseNozzlePropertiesConfiguration config = (FirehoseNozzlePropertiesConfiguration)context.getBean(FirehoseNozzlePropertiesConfiguration.class);
		
		assertNotNull(config);
		
		FirehoseProperties props = config.firehoseProperties(context);
		
		assertNotNull(props);

		assertEquals(props.getApiEndpoint(), EnableFirehoseNozzle.DEFAULT_API_ENDPOINT);
		assertEquals(props.getUsername(), EnableFirehoseNozzle.DEFAULT_USERNAME);
		assertEquals(props.getPassword(), EnableFirehoseNozzle.DEFAULT_PASSWORD);
		assertEquals(props.isSkipSslValidation(), new Boolean(EnableFirehoseNozzle.DEFAULT_SKIP_SSL_VALIDATION).booleanValue());
		
		assertTrue(props.isValidConfiguration());
		
		thrown.expect(NoSuchBeanDefinitionException.class);
		FirehoseReader reader = (FirehoseReader)context.getBean(FirehoseReader.class);
		
	}
	
}
