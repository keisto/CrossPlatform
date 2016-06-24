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
    var refresher : NSTimer!
    
    // Actions
    @IBAction func buttonClick (sender : UIButton) {
        // On Click Action by Tag
        switch (sender.tag) {
        case 0:
            // Tag 0 == Edit
            if (reachStatus != NOCONNECTION) {
                self.performSegueWithIdentifier("editPush", sender: nil)
            } else {
                self.errorLabel.text! = "Please check internet connection."
            }
            break;
        case 1:
            // Tag 1 == Delete
            if (reachStatus != NOCONNECTION) {
                if (uidValue != "") {
                    self.firebase.child("users").child(uidValue).child("firstname").removeValue()
                    self.firebase.child("users").child(uidValue).child("lastname").removeValue()
                    self.firebase.child("users").child(uidValue).child("age").removeValue()
                    self.firstname.text = ""
                    self.lastname.text = ""
                    self.age.text = ""
                }
            } else {
                self.errorLabel.text! = "Please check internet connection."
            }
            break;
        case 2:
            // Tag 2 == Logout
            try! FIRAuth.auth()!.signOut()
            NSUserDefaults.standardUserDefaults().removeObjectForKey("email")
            NSUserDefaults.standardUserDefaults().removeObjectForKey("password")
            self.performSegueWithIdentifier("logoutPush", sender: nil)
            break;
        default:
            break;
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Task(s)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: #selector(ViewController.reachStatusChanged), name: "ReachStatusChanged", object: nil)
        if(reachStatus != NOCONNECTION) {
            loadUser()
            refresher = NSTimer.scheduledTimerWithTimeInterval(10, target: self, selector: #selector(SelfViewController.refreshData), userInfo: nil, repeats: true)
        }
    }
    
    func reachStatusChanged() {
        if (reachStatus == NOCONNECTION) {
            self.errorLabel.text = "Internet Not Available."
            refresher.invalidate()
            firebase.removeAllObservers()
        } else {
            self.errorLabel.text = ""
            loadUser()
            refresher = NSTimer.scheduledTimerWithTimeInterval(10, target: self, selector: #selector(SelfViewController.refreshData), userInfo: nil, repeats: true)
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
                        self.email.text = snapshot.value!["email"] as? String
                        self.uidValue = user!.uid
                    })
                    // Add Listener For Changeing Data
                    self.firebase.child("users").child((FIRAuth.auth()?.currentUser?.uid)!).observeEventType(.Value, withBlock: { (snapshot) in
                        // Get user value
                        self.firstname.text = snapshot.value!["firstname"] as? String
                        self.lastname.text = snapshot.value!["lastname"] as? String
                        self.age.text = snapshot.value!["age"] as? String
                    })
                    self.firebase.keepSynced(true)
                } else {
                    // Something Went Wrong
                    self.errorLabel.text = "Please Enter Data."
                }
            }

        }
    }
    
    func refreshData() -> Void {
        if (reachStatus != NOCONNECTION) {
            self.firebase.child("users").child((FIRAuth.auth()?.currentUser?.uid)!).observeSingleEventOfType(.Value, withBlock: { (snapshot) in
                // Get user value
                self.firstname.text = snapshot.value!["firstname"] as? String
                self.lastname.text = snapshot.value!["lastname"] as? String
                self.age.text = snapshot.value!["age"] as? String
            })
        }
    }
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        return false
    }
    
    override func viewDidDisappear(animated: Bool) {
        refresher.invalidate()
        firebase.removeAllObservers()
    }
}