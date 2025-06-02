package com.smart.controller;

import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
public class forgotController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailservice;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;

     @RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
     
     
     @PostMapping("/send-otp")
 	public String sendOTP(@RequestParam("email")String email,HttpSession session)
 	{
    	 System.out.println("EMAIL"+email);
    	
    	 
    	 //generate otp of 4 digit
    	 
    	 Random random=new Random(1000);
    	 
    	int otp= random.nextInt(99999);
    	 
    	System.out.println("OTP" +otp);
    	
    	//write code for sending otp to email
    	String subject="OTP from SCM";
    	String message="<div style='border:1px solid #e2e2e2; padding:20px'>"
    			      +"<h1>"
    			      +"OTP is "
    			      +"<b>"+otp
    			      +"</n>"
    			      +"</h1>"
    			      +"</div>";
    			
    	String to=email;
    	 
    boolean flag=this.emailservice.sendEmail(subject,message,to);
    
    
    if(flag)
    {
        session.setAttribute("myotp", otp);
        session.setAttribute("email",email);
        session.setAttribute("message", "We have sent OTP to email"); // ✅ success message
     // After using the message in controller (optional)

        
        return "verify_otp";
    }
    else {
        session.setAttribute("message","Something went wrong"); // ✅ error message
     // After using the message in controller (optional)


        return "forgot_email_form";
    }

 	}
     
     
     //verify otp
     @PostMapping("/verify-otp")
     public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
     {
    	 int myotp=(int)session.getAttribute("myotp");
        String email=(String)session.getAttribute("email");
        
        if(myotp==otp)
        {
        	//password change form
        	 
        	User user=this.userRepository.getUserByUserName(email);
        	
        	if(user==null)
        	{
        		//send error message
        		
        		session.setAttribute("message", "User doesn't exist with this email");
        		return "forgot_email_form";
        	}else {
        		//send change password form
        		
        		
        	   
        	}
        	
        	return "password_change_form";
        }else {

        	session.setAttribute("message", "You have entered wrong otp");

        	return "verify_otp";
        	
        }
     }
     
     //change password
     @PostMapping("/change-password")
     public String changePassword(@RequestParam("newPassword")String newPassword,HttpSession session) {
         //TODO: process POST request
    	 String email=(String)session.getAttribute("email");
    	User user= this.userRepository.getUserByUserName(email);
          user.setPassword(this.bcrypt.encode(newPassword));
         this.userRepository.save(user);
         
   return"redirect:/signin?change=password changed successfully";
     }
     
     
     
}
