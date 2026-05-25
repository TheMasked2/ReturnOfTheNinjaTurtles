// package org.turtleshop.api.controller;

// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.turtleshop.api.config.TurtleShopUserDetails;
// import org.turtleshop.api.modules.customer.model.Customer;
// import org.turtleshop.api.modules.customer.service.CustomerService;

// @RestController
// @RequestMapping("/api/users")
// public class UserController {

//     private final CustomerService customerService;

//     public UserController(CustomerService customerService) {
//         this.customerService = customerService;
//     }

//     @GetMapping("/me")
//     public ResponseEntity<Customer> getMyProfile(@AuthenticationPrincipal TurtleShopUserDetails userDetails) {
//         if (userDetails == null) {
//             return ResponseEntity.status(401).build();
//         }
//         String userEmail = userDetails.getUsername();
//         Customer customer = customerService.findCustomerByEmail(userEmail);
//         if (customer == null) {
//             return ResponseEntity.notFound().build();
//         }
//         return ResponseEntity.ok(customer);
//     }
// }
