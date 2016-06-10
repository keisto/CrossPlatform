//
//  ViewController.swift
//  CrossPlatform
//
//  Created by buNny on 6/7/16.
//  Copyright © 2016 Tony Keiser. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    // Variables
    @IBOutlet weak var email : UITextField!
    @IBOutlet weak var password : UITextField!
    @IBOutlet weak var signup : UIButton!
    @IBOutlet weak var login : UIButton!
    @IBOutlet weak var errorLabel: UILabel!
    
    let firebase = FIRDatabase.database().reference()
    
    var saveEmail : String = ""
    var savePassw : String = ""
    
    // Actions
    @IBAction func buttonClick (sender : UIButton) {
        // On Click Action by Tag
        switch (sender.tag) {
        case 0:
            // Tag 0 == Sign Up
            if (self.email.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
                self.password.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "") {
                self.errorLabel.text! = "Please enter BOTH Email and Password."
            } else {
                self.errorLabel.text! = ""
                signupAction(self.email.text!, password: self.password.text!)
            }
            break;
        case 1:
            // Tag 1 == Login
            if (self.email.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
                self.password.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "") {
                self.errorLabel.text! = "Please enter BOTH Email and Password."
            } else {
                self.errorLabel.text! = ""
                loginAction(self.email.text!, password: self.password.text!)
            }
            break;
        default:
            break;
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Task(s)
            
        // Check if User has a login
        if (loadUser()) {
            // User Found Move to SelfView
            saveEmail = NSUserDefaults.standardUserDefaults().stringForKey("email")!
            savePassw = NSUserDefaults.standardUserDefaults().stringForKey("password")!
            loginAction(saveEmail, password: savePassw)
            performSegueWithIdentifier("loginPush", sender: nil)
        } else {
            // User NOT Found ... So stay for awhile
        }
    }

    // Login Action
    func loginAction (email: String, password: String) -> Void {
        FIRAuth.auth()?.signInWithEmail(email, password: password) { (user, error) in
            if (error == nil) {
                // Login Success
                self.errorLabel.text = "Login Successful!"
                // Save Email & Passowrd
                NSUserDefaults.standardUserDefaults().setValue(email, forKeyPath: "email")
                NSUserDefaults.standardUserDefaults().setValue(password, forKeyPath: "password")
                NSUserDefaults.standardUserDefaults().synchronize()
                self.performSegueWithIdentifier("loginPush", sender: nil)
            } else {
                // Something Went Wrong
                self.errorLabel.text = error?.localizedDescription
            }
        }
    }
    
    // Sign Up Action
    func signupAction(email: String, password: String) -> Void {
        FIRAuth.auth()?.createUserWithEmail(email, password: password) { (user, error) in
            if (error == nil) {
                // User Signin Success
                self.errorLabel.text = "Sign Up Successful!"
                self.firebase.child("users").child(user!.uid).setValue(["email": email])
                // Save Email & Passowrd
                NSUserDefaults.standardUserDefaults().setValue(email, forKeyPath: "email")
                NSUserDefaults.standardUserDefaults().setValue(password, forKeyPath: "password")
                NSUserDefaults.standardUserDefaults().synchronize()

            } else {
                // Something Went Wrong
                self.errorLabel.text = error?.localizedDescription
            }
        }
    }
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        return false
    }
    
    func loadUser() -> Bool {
        if ((NSUserDefaults.standardUserDefaults().valueForKey("email")) != nil) {
            return true
        }
        return false
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}