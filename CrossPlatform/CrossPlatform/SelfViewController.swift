//
//  SelfViewController.swift
//  CrossPlatform
//
//  Created by buNny on 6/7/16.
//  Copyright © 2016 Tony Keiser. All rights reserved.
//

import Foundation
import UIKit

class SelfViewController : UIViewController {
    
    // Variables
    @IBOutlet weak var delete : UIButton!
    @IBOutlet weak var edit : UIButton!
    @IBOutlet weak var logout : UIButton!
    @IBOutlet weak var firstname : UILabel!
    @IBOutlet weak var lastname : UILabel!
    @IBOutlet weak var email : UILabel!
    @IBOutlet weak var age : UILabel!
    @IBOutlet weak var errorLabel: UILabel!
    @IBOutlet weak var sessionLabel: UILabel!
    
    let firebase = FIRDatabase.database().reference()
    var uidValue : String = ""
    
    // Actions
    @IBAction func buttonClick (sender : UIButton) {
        // On Click Action by Tag
        switch (sender.tag) {
        case 0:
            // Tag 0 == Edit
            break;
        case 1:
            // Tag 1 == Delete
//            if (self.firstname.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
//                self.lastname.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "" ||
//                self.age.text!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet()) == "") {
////                self.firebase.child("users").child(uidValue).removeValue("firstname")
////                self.firebase.child("users").child(uidValue).removeValue("lastname")
////                self.firebase.child("users").child(uidValue).dele
//            }
//        
            
            break;
        case 2:
            // Tag 2 == Logout
            try! FIRAuth.auth()!.signOut()
            NSUserDefaults.standardUserDefaults().removeObjectForKey("email")
            NSUserDefaults.standardUserDefaults().removeObjectForKey("password")
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
                        self.email.text = snapshot.value!["email"] as? String
                    })
                } else {
                    // Something Went Wrong
                    self.errorLabel.text = "Please Enter Data."
                }
            }

        }
    }
}