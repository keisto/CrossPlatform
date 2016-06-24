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
            if (reachStatus != NOCONNECTION) {
                if (self.email.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
                    self.password.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "") {
                    self.errorLabel.text! = "Please enter BOTH Email and Password."
                } else if (Int(self.password.text!) >= 6) {
                    self.errorLabel.text! = "Password must be at LEAST 6 characters."
                } else if (!validEmail(self.email.text!)) {
                    self.errorLabel.text! = "Invalid email format."
                } else {
                self.errorLabel.text! = ""
                signupAction(self.email.text!, password: self.password.text!)
                }
            } else {
                self.errorLabel.text! = "Please check internet connection."
            }
            break;
        case 1:
            // Tag 1 == Login
            if (reachStatus != NOCONNECTION) {
                if (self.email.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
                    self.password.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "") {
                    self.errorLabel.text! = "Please enter BOTH Email and Password."
                } else {
                    self.errorLabel.text! = ""
                    loginAction(self.email.text!, password: self.password.text!)
                }
            } else {
                self.errorLabel.text! = "Please check internet connection."
            }
            break;
        default:
            break;
        }
    }
    
    func validEmail(checkEmail: String) -> Bool {
        let emailStyle = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"
        return NSPredicate(format:"SELF MATCHES %@", emailStyle).evaluateWithObject(checkEmail)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Task(s)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: #selector(ViewController.reachStatusChanged),
                                                         name: "ReachStatusChanged", object: nil)
            
        // Check if User has a login
        if (loadUser()) {
            // User Found Move to SelfView
            saveEmail = NSUserDefaults.standardUserDefaults().stringForKey("email")!
            savePassw = NSUserDefaults.standardUserDefaults().stringForKey("password")!
            print(saveEmail)
            if(reachStatus != NOCONNECTION) {
                // If Network, Check User. If Not, Trust User... Check Later
                loginAction(saveEmail, password: savePassw)
            } else {
                self.performSegueWithIdentifier("loginPush", sender: nil)
            }
        } else {
            // User NOT Found ... So stay for awhile
        }
    }
    
    func reachStatusChanged() {
        if (reachStatus == NOCONNECTION) {
            self.errorLabel.text = "Internet NOT available."
        } else {
            self.errorLabel.text = ""
            if (loadUser()) {
                // User Found Move to SelfView
                saveEmail = NSUserDefaults.standardUserDefaults().stringForKey("email")!
                savePassw = NSUserDefaults.standardUserDefaults().stringForKey("password")!
                if(reachStatus != NOCONNECTION) {
                    // If Network, Check User. If Not, Trust User... Check Later
                    loginAction(saveEmail, password: savePassw)
                } else {
                    self.performSegueWithIdentifier("loginPush", sender: nil)
                }
            }
        }
    }

    // Login Action
    func loginAction (email: String, password: String) -> Void {
        FIRAuth.auth()?.signInWithEmail(email, password: password) { (user, error) in
            if (error == nil) {
                // Login Success
                self.errorLabel.text = "Login Successful!"
                // Save Email & Passowrd
                if (email != "" && password != "") {
                    NSUserDefaults.standardUserDefaults().setValue(email, forKeyPath: "email")
                    NSUserDefaults.standardUserDefaults().setValue(password, forKeyPath: "password")
                    NSUserDefaults.standardUserDefaults().synchronize()
                    self.performSegueWithIdentifier("loginPush", sender: nil)
                }
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
                if (email != "" && password != "") {
                    NSUserDefaults.standardUserDefaults().setValue(email, forKeyPath: "email")
                    NSUserDefaults.standardUserDefaults().setValue(password, forKeyPath: "password")
                    NSUserDefaults.standardUserDefaults().synchronize()
                }

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