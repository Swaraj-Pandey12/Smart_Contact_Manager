package com.smart.controller;


import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bryptPasswordEncoder;
	


    
	@Autowired
	public UserRepository userRepository;
	
	@Autowired 
	private ContactRepository contactRepository;
	//method for addding to common data to response
	@ModelAttribute
	public void addcommondata(Model model,Principal principal) {
	String userName=principal.getName();
	System.out.println("USERNAME "+userName);
	
	//get the user using username(Email)
	
	User user=userRepository.getUserByUserName(userName);
	
	
	System.out.println("USER "+user);
	
	model.addAttribute("user",user);
}


 //home dashboard
   @RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
	   

	   model.addAttribute("title","User dashboard");
		
		return "norml/user_dashboard";
	 
	}
	
	//open add form handler
	
   @GetMapping("/add-contact")
   public String openAddContactForm(Model model, HttpSession session) {
       Message message = (Message) session.getAttribute("message");

	   Object obj = session.getAttribute("message");
	   if (obj instanceof Message) {
	       model.addAttribute("message", (Message) obj);
	       session.removeAttribute("message");
	   }


       model.addAttribute("title", "Add Contact");
       model.addAttribute("contact", new Contact());
     //  session.setAttribute("message", new Message("Contact added successfully", "success"));
       return "norml/add_contact_form";  // spelling is "norml" as per your file structure
   }

	//processing ADD contact form
	
	@PostMapping("/user/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
        
		try {
		String name=principal.getName();
		 User user= this.userRepository.getUserByUserName(name);
		 
		 //processing and uploading file
		 
		 if(file.isEmpty()) {
			 //if the file is empty then try our message 
			 System.out.println("Image is empty");
			 contact.setImage("contact.png");
			 
			 //message success
			
			 
		 }
		 else {
			 //file the file to the folder and update the name to contact
			 contact.setImage(file.getOriginalFilename());
			 File saveFile=new ClassPathResource("static/image").getFile();
			Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			 
			 
			 Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
			 System.out.println("Image is uploaded");
		 }
		  
		  contact.setUser(user);
          user.getContacts().add(contact) ;
          
          this.userRepository.save(user);
		 
          
		System.out.println("DATA "+contact);
		
		 System.out.println("Addded to databases");
		 
		 session.setAttribute("message",new Message("Your contact is added","success"));
		}catch(Exception e) {
			e.printStackTrace();
			 session.setAttribute("message",new Message("Something went wrong","danger"));
		}
		return "norml/add_contact_form";
	}
	
	//show contacts handler
	//per page =5[n]
	//current page=0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContatcs(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts");
		//contact ki list ko  bhejni hai
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		//currentPage
		//contact perPage 
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "norml/show_contacts";
	}
	
	//showing particular contact details
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model m,Principal principal) {
		System.out.println("CID" +cid);
		// In UserController.java at or around line 170
		Optional<Contact> optionalContact = this.contactRepository.findById(cid); // Assuming 'id' is passed to the method

		if (optionalContact.isPresent()) {
		    Contact contact = optionalContact.get();
		    m.addAttribute("contact", contact);
		    m.addAttribute("title",contact.getName());
		    
		    // ... rest of your logic
		}
	    String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
	    
	   
		return "norml/contact_details";
	}
	
	//delete contact handler 
	// In com.smart.controller.UserController

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model m, Principal principal, HttpSession session){
	    Optional<Contact> contactOptional = this.contactRepository.findById(cid);

	    if (contactOptional.isEmpty()) {
	        session.setAttribute("message", new Message("Contact not found!", "alert-danger"));
	        return "redirect:/user/show-contacts/0";
	    }

	    Contact contactToDelete = contactOptional.get(); // Safe now, as we checked isEmpty()

	    String userName = principal.getName();
	    User currentUser = this.userRepository.getUserByUserName(userName);

	    // Security check: Ensure the contact belongs to the current user
	    // Assuming Contact has a ManyToOne back to User, or that a Contact can only be associated with one User.
	    // If it's a pure Many-to-Many, this check might need to be refined.
	    // For now, let's assume `contact.getUser()` provides the primary owner.
	    if (currentUser.getId() == contactToDelete.getUser().getId()) {

	        // If it's a Many-to-Many, you need to remove the association from the User's side.
	        // Assuming User has a Set<Contact> contacts:
	        currentUser.getContacts().remove(contactToDelete);
	        this.userRepository.save(currentUser); // This should remove the entry from user_contacts table

	        // Now, attempt to delete the contact entity itself.
	        // This will only succeed if this contact is no longer referenced by ANY user.
	        // If other users could also "have" this contact (true Many-to-Many), this line will fail
	        // if those other users still reference it.
	        // If a contact is meant to be uniquely owned by one user, then the above `save(currentUser)`
	        // might be enough to cascade the deletion if your User entity has `orphanRemoval=true` for Contacts.
	        // However, if it's a strict Many-to-Many, you might need a different strategy for the Contact deletion itself.
	        // For example, only delete the Contact if no other User references it, or force-delete.

	        // Given the error, the association in `user_contacts` is the issue.
	        // The `user.getContacts().remove(contactToDelete);` and `userRepository.save(user);`
	        // should resolve the join table entry if `User` is the owning side and configured correctly.
	        // If it still fails, it means the `contactToDelete` is still referenced by *another* User,
	        // or your JPA mappings aren't correctly cascading the removal from the join table.

	        // Let's retry with just deleting from the repository, but ensure the mappings are correct.
	        this.contactRepository.delete(contactToDelete); // This line is what caused the DataIntegrityViolationException

	        session.setAttribute("message", new Message("contact deleted successfully", "success"));

	    } else {
	        // Handle case where user is not authorized to delete this contact
	        session.setAttribute("message", new Message("You are not authorized to delete this contact!", "alert-danger"));
	    }

	    return "redirect:/user/show-contacts/0";
	}	
	
	//open update form
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m)
	{
		m.addAttribute("title","update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact",contact);
		return "norml/update_form";
	}
	
	
	
	//update contact handler
		

@RequestMapping(value="/process-update",method=RequestMethod.POST)
public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
	
	
	try {
		//old contact details
		Contact oldcontactDetail=this.contactRepository.findById(contact.getCid()).get();
		
		
		//image
		
		
		if(!file.isEmpty())
		{
		 //file work
			//rewrite 
			
			//delete old photo 
			
			 File deleteFile=new ClassPathResource("static/image").getFile(); File
			 
			 file1=new File(deleteFile,oldcontactDetail.getImage()); 
			 file1.delete();
			 
				 
				 
			
			
			//update new photo
			
			 File saveFile=new ClassPathResource("static/image").getFile();
				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				 
				 
				 Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				 
				 contact.setImage(file.getOriginalFilename());
				 
			
		}else
		{
			contact.setImage(oldcontactDetail.getImage());
		}
		
	
	
	User user=this.userRepository.getUserByUserName(principal.getName());
	contact.setUser(user);
	
	this.contactRepository.save(contact);
	
	session.setAttribute("message", new Message("your contact is updated","success"));
	}
	catch(Exception e) {
		e.printStackTrace();
	}
	
	System.out.println("Contact Name"+contact.getName());

	System.out.println("Contact ID"+contact.getCid());
	
	
	return"redirect:/user/"+contact.getCid()+"/contact";
	
			}
	
	//your profile handler

   @GetMapping("/profile")
  public String yourprofile(Model m) {
	  m.addAttribute("title","profile page");
	  return "norml/profile";
  }
   
   
   //open setting handler
   @GetMapping("/settings")
   public String openSetting() {
	   return "norml/settings";
   }
   
   //change password handler
   @PostMapping("/change-password")
   public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
	   
	   System.out.println("OLD PassWord"+oldPassword);
	   System.out.println("New PassWord"+newPassword);
	   
	   
	   String userName=principal.getName();
	   
	  User currentUser= this.userRepository.getUserByUserName(userName);
	  
	  System.out.println(currentUser.getPassword());
	  
	  
	  if(this.bryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())){
		  //change the password
		  
		  currentUser.setPassword(this.bryptPasswordEncoder.encode(newPassword));
		  this.userRepository.save(currentUser);
		  session.setAttribute("message",new Message("your password has successfully changed","alert-success"));
	  }
	  else {
		  //error
		  session.setAttribute("message",new Message("please enter correct old password","danger"));
		   return "redirect:/user/settings";

	  
	  }
	   return "redirect:/user/index";
   }
   

}
