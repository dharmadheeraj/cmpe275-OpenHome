package com.cmpe275.openhome.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cmpe275.openhome.model.Property;
import com.cmpe275.openhome.service.AmazonClient;
import com.cmpe275.openhome.service.EmailService;
import com.cmpe275.openhome.service.PropertyUploadService;
import com.cmpe275.openhome.utils.EmailUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class PropertyUploadController {

	@Autowired
    PropertyUploadService propertyUploadService;
    private Long id;
    
    private AmazonClient amazonClient;
    
    @Autowired
	EmailService emailService;

    @Autowired
    PropertyUploadController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    
    
    @ResponseBody
    @RequestMapping(method=RequestMethod.POST, value = "/uploadProperty")
    public ResponseEntity<Property> uploadProperty(@RequestPart(value = "data") String data,@RequestPart(value = "files") MultipartFile[] files) throws JsonMappingException, JsonProcessingException{
    	String response = "";
    	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	Property prop;
    	
    	try {
    		prop = mapper.readValue(data, Property.class);
    		for(MultipartFile file : files)
            {
            	System.out.println("started upload");
            	response += this.amazonClient.uploadFile(file);
            	System.out.println("ended upload : " + response );
            }
            
        	prop.setImages(response);
            
            System.out.println("Images sting: " + prop.getImages());
    	}
    	catch (JsonMappingException e){
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    	}
    	catch (Exception e){
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);  
    	}
        
        propertyUploadService.uploadProperty(prop);
        String postPropertyHost = EmailUtility.propertyPostMessageHost();
        emailService.sendEmail(prop.getHost_email(), postPropertyHost, "Property Posted!!");
    	return ResponseEntity.ok(prop);
    }
    
    @RequestMapping(method=RequestMethod.PATCH, value = "/updateProperty")
    public ResponseEntity<Property> updateProperty(@RequestBody Property data) throws ParseException{
    	try {
    		Property updated = propertyUploadService.updateProperty(data);
    		String updatePropertyHost = EmailUtility.propertyUpdateMessageHost();
            emailService.sendEmail(updated.getHost_email(), updatePropertyHost, "Property Updated!!");
            return ResponseEntity.ok(updated);
    	}
    	catch (Exception e){
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);  
    	}  
    }
    
    @RequestMapping(method=RequestMethod.POST, value = "/deleteProperty")
    public ResponseEntity<Property> deleteProperty(@RequestBody Property data) throws ParseException{
    	try {
    		System.out.println("hgjv");
    		Property deleted  = propertyUploadService.deleteProperty(data);
	        String deleteMessage = EmailUtility.propertyDeleteMessageHost();
	        emailService.sendEmail(deleted.getHost_email(), deleteMessage, " Property Deleted!!");
	    	return ResponseEntity.ok(deleted);
    	}
    	catch (Exception e){
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);  
    	}  
       
    }
    
    
}