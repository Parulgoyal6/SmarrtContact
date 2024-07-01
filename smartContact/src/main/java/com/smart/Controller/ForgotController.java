package com.smart.Controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		System.out.println("EMAIL: " + email);
		
		//generating otp 4 digit number
		
		Random random= new Random();
		int otp=random.nextInt(9000);
		System.out.println("OTP: " + otp);
		
		
		//code sent otp to your email
		
		String subject="OTP From SCM";
		String message=""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+"<h1>"
				+"OTP is"
				+"<b>" + otp
				+"</n>"
				+"</h1>"
				+"</div>";
				
		
		String to=email;
		boolean flag=this.emailService.sendEmail(subject, message, to);
			
		if(flag) {
			
			session.setAttribute("Myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		}
		
		else {
			session.setAttribute("message", "Check Your email id!!");
			return "forgot_email_form";
		}
		}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int Myotp= (int)session.getAttribute("Myotp");
		String email=(String)session.getAttribute("email");
		if(Myotp==otp) {
			//password changed form
			
			User user=this.userRepository.getUserByUserName(email);
			
			
			if(user==null) {
				//send error message
				
				session.setAttribute("message", "user does not exist with this email!!");
				
				return "forgot_email_form";
			}
			else {
				
				// send change password form
				
				//return "password_change_form";
			}
			
			return "password_change_form";
		}
		else {
			
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify_otp";
		}
		
	}
	
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
	
		String email=(String)session.getAttribute("email");
		User user=this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully...";
	//return "signin";
	}
	
		
}
