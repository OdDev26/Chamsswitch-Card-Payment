package com.vfd.Zest.Test.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vfd.Zest.Test.request.PaymentRequest;
import com.vfd.Zest.Test.response.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Controller
public class ViewController {
   @Autowired
   RestTemplate restTemplate;
   ObjectMapper objectMapper;

   @Value(value = "${vfd.token}")
   String vfdToken;


   @GetMapping("/zest-payment")
   public String nonTokenizedPayment(Model model) throws JsonProcessingException {
      PaymentRequest paymentRequest = new PaymentRequest();
      model.addAttribute("paymentRequest",paymentRequest);
      return "non-tokenized-payment-form";
   }

   @GetMapping("/tokenized")
   public String tokenizedPayment(Model model) throws JsonProcessingException {
      PaymentRequest paymentRequest = new PaymentRequest();
      model.addAttribute("paymentRequest",paymentRequest);
      return "tokenized-payment-form";
   }

   @GetMapping("/chamsswitch-payment")
   public String chamsswitchPayment(Model model) throws JsonProcessingException {
      PaymentRequest paymentRequest = new PaymentRequest();
      model.addAttribute("paymentRequest",paymentRequest);
      return "chamsswitch-payment-form";
   }

   @PostMapping("/process/non-tokenize-payment")
   public String processNonTokenizedPayment(@ModelAttribute("paymentRequest")PaymentRequest paymentRequest,Model model) throws JsonProcessingException {

         paymentRequest.setShouldTokenize(true);
         paymentRequest.setUseExistingCard(false);
         Model model1 = processZestNonTokenizedPayment(paymentRequest, model);

         // Check if redirectHtml exists
         String redirectHtml = (String) (model1.getAttribute("redirectHtml"));
         System.out.println("Redirect html: " +redirectHtml);
         String code = (String) (model1.getAttribute("code"));
         System.out.println("Code: " +code);

         if(redirectHtml != null && !redirectHtml.isEmpty()){
             return "03-page";
         }
         else if(code != null && code.equals("01")){
            return "01-page";
         }
         else if(code != null && code.equals("02")){
            return "02-page";
         }

         else {
            System.out.println("Unable to handle payment response code");
            System.out.println(model1.toString());
            return "01-payment-failed.html";
         }

   }
   @PostMapping("/process/chamsswitch-payment")
   public String processChammswitchPayment(@ModelAttribute("paymentRequest")PaymentRequest paymentRequest,Model model) throws JsonProcessingException {
      Model model2 = processChammswitchPayment2(paymentRequest, model);
      return "complete-chamsswitch-payment";
   }

   @PostMapping("/otp-validate")
   public String validateOtp(@ModelAttribute("paymentRequest")PaymentRequest paymentRequest,Model model) throws JsonProcessingException {
         String message = validateOtp(paymentRequest);
         if(message.equals("Success")) return "01-payment-success";
            model.addAttribute("message",message);
            return "01-payment-failed";
   }

   private String validateOtp(PaymentRequest paymentRequest) throws JsonProcessingException {
      objectMapper = new ObjectMapper();

      String redirectHtml = "";
      // make call to baas pay initiate endpoint
      String url = "https://api-apps.vfdbank.systems/vtech-wallet/api/v1/baas-cards/validate-otp";
      String payload = objectMapper.writeValueAsString(paymentRequest);
      System.out.println("Payload: " + payload);
      HttpHeaders headers = new HttpHeaders();
      headers.add("accesstoken", vfdToken);
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<?> httpEntity = new HttpEntity<>(payload, headers);
      try {
         ResponseEntity<PaymentResponse> paymentResponseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PaymentResponse.class);
         System.out.println("Payment response: " + paymentResponseEntity.getBody().getData());
         return paymentResponseEntity.getBody().getMessage();

      }catch (Exception e){
         System.out.println("Exception: "+e.getMessage());
         return e.getMessage();
      }
   }

   private Model processZestNonTokenizedPayment(PaymentRequest paymentRequest, Model model) throws JsonProcessingException {
      objectMapper = new ObjectMapper();
      String redirectHtml = "";
      // make call to baas pay initiate endpoint
      String url = "https://api-apps.vfdbank.systems/vtech-wallet/api/v1/baas-cards/initiate/payment";
      String payload = objectMapper.writeValueAsString(paymentRequest);
      System.out.println("Payload: "+payload);
      System.out.println("Vfd token: "+vfdToken);
      HttpHeaders headers = new HttpHeaders();
      headers.add("accesstoken", vfdToken);
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<?> httpEntity = new HttpEntity<>(payload, headers);
      try {
         ResponseEntity<PaymentResponse> paymentResponseEntity  = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PaymentResponse.class);
         System.out.println("Payment response: "+paymentResponseEntity.getBody().getData());
         if (!(paymentResponseEntity.getBody().getData().getRedirectHtml().isEmpty())) {
            // Read the redirect html
            model.addAttribute("redirectHtml",paymentResponseEntity.getBody().getData().getRedirectHtml());
         }
         else if(paymentResponseEntity.getBody().getData().getCode().equals("02")){
            String acsUrl = paymentResponseEntity.getBody().getData().getAcsUrl();
            String jwt = paymentResponseEntity.getBody().getData().getJwt();
            String md = paymentResponseEntity.getBody().getData().getMd();
            model.addAttribute("acsUrl",acsUrl);
            model.addAttribute("jwt",jwt);
            model.addAttribute("md",md);
            model.addAttribute("code",paymentResponseEntity.getBody().getData().getCode());
         }
         else if(paymentResponseEntity.getBody().getData().getCode().equals("01")){
            PaymentRequest paymentRequest1 = new PaymentRequest();
            paymentRequest1.setReference(paymentRequest.getReference());
            model.addAttribute("paymentRequest",paymentRequest1);
            model.addAttribute("code",paymentResponseEntity.getBody().getData().getCode());
         }
         else {
            System.out.println("Cannot process api response");
         }
      }catch (HttpClientErrorException.BadRequest | NullPointerException badRequest){
          System.out.println("exception message: "+badRequest.getMessage());
          model.addAttribute("response",badRequest.getMessage());
      } catch (HttpServerErrorException httpClientErrorException){
         System.out.println("Exception from api: "+httpClientErrorException.getLocalizedMessage());
         model.addAttribute("response",httpClientErrorException.getLocalizedMessage());
      }


      // pass the redirect html to a js file using payment
      return model;
   }
   private Model processChammswitchPayment2(PaymentRequest paymentRequest, Model model) throws JsonProcessingException {
      objectMapper = new ObjectMapper();
      // make call to baas pay initiate endpoint
      // Prod url
//      String url = "https://api-apps.vfdbank.systems/vtech-wallet/api/v1/baas-cards/initiate/payment";
      // Dev url
      String url = "https://api-devapps.vfdbank.systems/vtech-wallet/api/v1.1/baas-cards/initiate/payment";
      String payload = objectMapper.writeValueAsString(paymentRequest);
      System.out.println("Payload: "+payload);
      System.out.println("Vfd token: "+vfdToken);
      HttpHeaders headers = new HttpHeaders();
      headers.add("accesstoken", vfdToken);
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<?> httpEntity = new HttpEntity<>(payload, headers);
      try {
         ResponseEntity<PaymentResponse> paymentResponseEntity  = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PaymentResponse.class);
         model.addAttribute("jwt",paymentResponseEntity.getBody().getData().getFormData().getOtherFormData().getJWT());
         model.addAttribute("url",paymentResponseEntity.getBody().getData().getFormData().getUrl());
       /*  System.out.println("Jwt: "+model.getAttribute("Jwt"));
         System.out.println("url: "+model.getAttribute("url"));*/
         System.out.println("Data: "+paymentResponseEntity.getBody().getData());
      }catch (HttpClientErrorException.BadRequest | NullPointerException badRequest){
         System.out.println("exception message: "+badRequest.getMessage());
         model.addAttribute("response",badRequest.getMessage());
      } catch (HttpServerErrorException httpClientErrorException){
         System.out.println("Exception from api: "+httpClientErrorException.getLocalizedMessage());
         model.addAttribute("response",httpClientErrorException.getLocalizedMessage());
      }

      return model;
   }

   public static void main(String[] args) {
      PaymentRequest paymentRequest = null;
      if(!(paymentRequest== null) && paymentRequest.getOtp().equals("123")){
         System.out.println("1");
      }
      else {
         System.out.println("2");
      }
   }
}
