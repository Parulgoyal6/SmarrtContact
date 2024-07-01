package com.smart.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName= principal.getName();
		System.out.println("USERNAME" + userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER" + user);
		model.addAttribute("user", user);
		
	}
	
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
		
		return "normal/user_dashboard";
	}
	
	
	//open add from handler
	@GetMapping("/add-contact")
	
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@RequestMapping(value="/process-contact", method = RequestMethod.POST)
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession Session) {
		try {
		String name=principal.getName();
		User user = this.userRepository.getUserByUserName(name);
	
		//processing and uploading file
		
		if(file.isEmpty()) {
			System.out.println("file is empty");
			
			contact.setImage("contact.png");
		}
		else
		{
			// upload the file to folder
			
			contact.setImage(file.getOriginalFilename());
			
			File saveFile =new ClassPathResource("static/img").getFile();
			
			Path path=Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("DATA " +contact);
		
		System.out.print("Added to database");
		
		
		//message success.....
	Session.setAttribute("message", new Message("Your Contact is added !! Add more...", "success"));
		}catch(Exception e) {
		System.out.println("ERROR " + e.getMessage());
		e.printStackTrace();
		
		//message error....
		
		Session.setAttribute("message", new Message("Something went wrong !! ", "danger"));
		}
		return "normal/add_contact_form";
	}
	 //show contacts handler
	//per page = 5[n]
	//current page = 0 [page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m, Principal principal) {
		
		m.addAttribute("title", "Show User Contacts");
		String userName=principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		 
		Pageable pageable=PageRequest.of(page, 3);
		Page<Contact>contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage" ,page);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		//User user = this.userRepository.getUserByUserName(userName);
		//List<Contact> contacts = user.getContacts();
		return "normal/show_contacts";
	}
	@RequestMapping("/{Cid}/contact")
	public String showContactDetail(@PathVariable("Cid")Integer Cid, Model model, Principal principal) {
		
		System.out.println("CID" +Cid);
		
		Optional<Contact>contactOptional=this.contactRepository.findById(Cid);
		Contact contact = contactOptional.get();
		
		//
		String userName=principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			
			model.addAttribute("title", contact.getName());
			model.addAttribute("contact", contact);
		 }
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{Cid}")
	public String deleteContact(@PathVariable("Cid") Integer Cid, Model model, HttpSession session, Principal principal) {
		Contact contact= this.contactRepository.findById(Cid).get();
		
		System.out.println("Contact" + contact.getCid());
		//contact.setUser(null);
		//check.... Assignment..
		//this.contactRepository.delete(contact);
		
		User user= this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Deleted");
		session.setAttribute("message" , new Message("Contact deleted Successfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	// Open Update form
	@PostMapping("/update-contact/{Cid}")
	public String UpdateForm(@PathVariable("Cid") Integer Cid, Model m) {
		
		m.addAttribute("title","update Contact");
		Contact contact = this.contactRepository.findById(Cid).get();
		
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//Update contact Handler
	@RequestMapping(value="/process-update",method= RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m,HttpSession session, Principal principal) {
		
		try {
			
			Contact oldcontactDetail=this.contactRepository.findById(contact.getCid()).get();
			
			if(!file.isEmpty()) {
				//file work
				
				//delete old photo
				
				File deleteFile =new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();
				
				//update new photo
				
				File saveFile =new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				
				contact.setImage(oldcontactDetail.getImage());
			}
			
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Message is updated...", "success"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT NAME "+ contact.getName());
		System.out.println("CONTACT Id "+ contact.getCid());
		
		return "redirect:/user/" + contact.getCid()+"/contact";
	}
	
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//Change Password Handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		
		System.out.println("OLD PASSWORD " + oldPassword );
		System.out.println("New PASSWORD " + newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		System.out.print(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword)); 
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password is successfully changed...", "success"));
		}
		else
		{
			//error ...
			
			session.setAttribute("message", new Message("Please Enter Correct old password !!...", "danger"));
			return "redirect:/user/settings";
			
		}
		
		return "redirect:/user/index";
	}
	
	
	
}