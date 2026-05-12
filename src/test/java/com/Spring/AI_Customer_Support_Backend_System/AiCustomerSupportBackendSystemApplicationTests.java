package com.Spring.AI_Customer_Support_Backend_System;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.RoleType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.TicketRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.UserRepository;
import com.Spring.AI_Customer_Support_Backend_System.Services.ToolService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest

class AiCustomerSupportBackendSystemApplicationTests {

	@Autowired
	private ToolService toolService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testTool()	{
//		System.out.println(toolService.searchCompanyPolicy("What are the security measures in your AI Customer Support System"));
	}

	@Test
	void makeagent()	{
		try	{
			User user = userRepository.findByEmail("rima@gmail.com").orElse(null);
			if(user==null)
				throw new Exception();
			user.setRole(RoleType.AGENT);
			userRepository.save(user);
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
	}

	@Test
	void makeadmin()	{
		try	{
			User user = userRepository.findByEmail("karmakarpriyanshu18@gmail.com").orElse(null);
			if(user==null)
				throw new Exception();
			user.setRole(RoleType.ADMIN);
			userRepository.save(user);
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
	}


}
