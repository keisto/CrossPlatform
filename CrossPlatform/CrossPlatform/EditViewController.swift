//
//  EditViewController.swift
//  CrossPlatform
//
//  Created by buNny on 6/7/16.
//  Copyright © 2016 Tony Keiser. All rights reserved.
//

import Foundation
import UIKit

class EditViewController : UIViewController {
    
    // Variables
    @IBOutlet weak var cancel : UIButton!
    @IBOutlet weak var save : UIButton!
    @IBOutlet weak var firstname : UITextField!
    @IBOutlet weak var lastname : UITextField!
    @IBOutlet weak var age : UITextField!
    @IBOutlet weak var errorLabel: UILabel!
    @IBOutlet weak var sessionLabel: UILabel!
    
    let firebase = FIRDatabase.database().reference()
    
    var userEmail : String = ""
    
    // Actions 
    @IBAction func buttonClick (sender : UIButton) {
        // On Click Action by Tag
        switch (sender.tag) {
        case 1:
            // Tag 1 == Save
            if (self.firstname.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
                self.lastname.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
                self.age.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "") {
                self.errorLabel.text! = "Please enter All fields."
            } else {
                self.errorLabel.text! = ""
                saveAction()
            }
            break;
        default:
            break;
        }

    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Task(s)
        loadUser()
    }
    
    func saveAction() -> Void {
        let email = NSUserDefaults.standardUserDefaults().stringForKey("email")
        let password = NSUserDefaults.standardUserDefaults().stringForKey("password")
        // Get Valid User 
        FIRAuth.auth()?.signInWithEmail(email!, password: password!) { (user, error) in
            if (error == nil) {
                self.firebase.child("users").child(user!.uid).setValue(["firstname": self.firstname.text!,
                    "lastname": self.lastname.text!, "age": self.age.text!, "email": self.userEmail])
               
//                self.performSegueWithIdentifier("loginPush", sender: nil)
            } else {
                // Something Went Wrong
                self.errorLabel.text = "Something went wrong."
            }
        }
    }
    
    func loadUser() -> Void {
        if ((NSUserDefaults.standardUserDefaults().valueForKey("email")) != nil) {
            let saveEmail = NSUserDefaults.standardUserDefaults().stringForKey("email")
            let savePassw = NSUserDefaults.standardUserDefaults().stringForKey("password")
            // Get Valid User
            FIRAuth.auth()?.signInWithEmail(saveEmail!, password: savePassw!) { (user, error) in
                if (error == nil) {
                    self.firebase.child("users").child(user!.uid).observeSingleEventOfType(.Value, withBlock: { (snapshot) in
                        // Get user value
                        self.sessionLabel.text = snapshot.value!["email"] as? String
                        self.firstname.text = snapshot.value!["firstname"] as? String
                        self.lastname.text = snapshot.value!["lastname"] as? String
                        self.age.text = snapshot.value!["age"] as? String
                        self.userEmail = (snapshot.value!["email"] as? String)!
        
                    })
                } else {
                    // Something Went Wrong
                    self.errorLabel.text = "Please Enter Data."
                }
            }
            
        }
    }
}
