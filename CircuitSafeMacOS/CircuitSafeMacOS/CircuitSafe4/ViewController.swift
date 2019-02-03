//
//  ViewController.swift
//  CircuitSafe4
//
//  Created by Jasper on 2019-02-02.
//  Copyright © 2019 Jasper. All rights reserved.
//

import Cocoa
import Alamofire
class ViewController: NSViewController {
    @IBOutlet weak var voltage: NSTextField!
    @IBOutlet weak var amps: NSTextFieldCell!
    @IBOutlet weak var minWidth: NSTextField!
    @IBOutlet weak var minDist: NSTextField!
    @IBOutlet weak var errorMessage: NSTextField!
    @IBOutlet weak var fineName: NSTextField!
    @IBOutlet weak var myImage: NSImageView!
    @IBOutlet weak var outputMessage: NSTextField!
    var filePicked = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.window?.title = "Circuit Safe"
    
//image.isHidden = true
        // Do any additional setup after loading the view.
    }

    override var representedObject: Any? {
        didSet {
        // Update the view, if already loaded.
        }
    }
    @IBAction func sumbitD(_ sender: Any) {
        
        let minimumDist = minDist.stringValue
        let volts = voltage.stringValue
        let amp = amps.stringValue
        let minimumWidth = minWidth.stringValue
        if Float(minimumWidth) ?? -1 == -1 || Float(volts) ?? -1 == -1 || Float(amp) ?? -1 == -1 || Float(minimumDist) ?? -1 == -1{
            errorMessage.stringValue = "Error please enter a vaild number"
        }else if !filePicked{
         errorMessage.stringValue = "please select a file"
        }else{
            errorMessage.stringValue = ""
            let minimumDistF = Int(minimumDist)!
            let voltsF = Int(volts)!
            let ampF = Int(amp)!
            let minimumWidthF = Int(minimumWidth)!
            

            let inputs : [String : Any] = [
                "min_dist": minimumDistF ,
                "min_width": minimumWidthF,
                "voltage":voltsF,
                "current":ampF
            ]

            
            Alamofire.request("http://35.230.167.95/compute", method: .post, parameters: inputs, encoding: JSONEncoding.default).responseJSON { response in
                if response.result.isSuccess {
                    print("worked")
                }
            }
            

            

        }

         let finalValues : [String:Any] = [
        "min_dist": 1 ,
        "min_width": 2,
        "voltage":3,
        "current":4
        ]
        Alamofire.request("http://35.230.167.95/compute",method: .post, parameters: finalValues,encoding:JSONEncoding.default).responseJSON { response in
            switch response.result {
            case .success(let value):
                print(response)
                let dict = response.result.value
                let finalOutput = dict as! NSDictionary
                print(finalOutput)
                if let currentRating = finalOutput["currentRating"] as? Double{
                    let currentRating = finalOutput["currentRating"] as? Double
                
                if let electricField = finalOutput["electricField"] as? Double{
                    var electricField = finalOutput["electricField"] as? Double
                
                if let propDelay = finalOutput["propDelay"] as? Double{
                    let propDelay = finalOutput["propDelay"] as? Double
                
                if let signalSpeed = finalOutput["signalSpeed"] as? Double{
                    let signalSpeed = finalOutput["signalSpeed"] as? Double
                    
                    self.outputMessage.stringValue = "Current rating: \(currentRating ?? 0) A\nelectric field: \(electricField ?? 0) N/C\nPropagation delay: \(propDelay ?? 0) s\nSignal speed: \(signalSpeed ?? 0) m/s"
                    
                    self.myImage.isHidden = false
                    }
                    }
                    }
                }

            case .failure(let error):
                debugPrint(response.result.error as Any)
                print ("error: \(error)")
            }
        }
      
//            Alamofire.request("http://35.230.167.95/compute").responseData { (response) in
//                if response.error == nil {
//                    print(response.result)
//                    var output = response.result// Show the downloaded image:\
//                }
//            }
        //}
    }

       
    
    
    
    
    
    @IBAction func upload(_ sender: Any) {
        if fineName.stringValue != ""{
            let allFileName = fineName.stringValue
            
            guard let gerberData = NSData(contentsOfFile: allFileName) else{
                print("error")
                return
            }
            uploadFiles(fileData: (gerberData as Data?)!,url:"http://35.230.167.95/test")
           self.filePicked = true
            // The image to dowload
                  let remoteImageURL = URL(string: "http://35.230.167.95/output.png")!
        
                    // Use Alamofire to download the image
                   Alamofire.request(remoteImageURL).responseData { (response) in
                           if response.error == nil {
                                  print(response.result)

                                // Show the downloaded image:
                                  if let data = response.data {
                                           self.myImage.image = NSImage(data: data)
                                  
                                    
                                                  }
                              }
                    }
        
            
        }else{
            errorMessage.stringValue = "please select a file"
        }
        
    }

    
    
    
    @IBAction func browseFiles(_ sender: Any) {
        
        let dialog = NSOpenPanel();
        
        dialog.title                   = "Please select your gerber files";
        dialog.showsResizeIndicator    = true;
        dialog.showsHiddenFiles        = false;
        dialog.canChooseDirectories    = true;
        dialog.canCreateDirectories    = true;
        dialog.allowsMultipleSelection = false;
        dialog.allowedFileTypes        = ["GTL"];
        
        if (dialog.runModal() == NSApplication.ModalResponse.OK) {
            let result = dialog.url // Pathname of the file
            
            if (result != nil) {
                let path = result!.path
                fineName.stringValue = path
            }
        } else {
            // User clicked on "Cancel"
            return
        }
    }
    // func uploadFiles(fileData: Data,progressCompletion: @escaping (_ percent: Float) -> Void) {
    func uploadFiles(fileData: Data, url: String){
        // 1
//        var fileData = [NSData, dataWithContentsOfFile,:fil options: 0 error: &error]
    

        // 2
        
        Alamofire.upload(multipartFormData: { multipartFormData in
            multipartFormData.append(fileData, withName: "gerber", fileName: "gerber.GTL", mimeType: "gerber/GTL")
        },
                         to: url,
                                                      method: .post,
                                     encodingCompletion: { encodingResult in
                                        switch encodingResult {
                                        case .success(let upload, _, _):
                                            upload.responseString { response in
                                                print(response)
                                            }
                                        case .failure(let encodingError):
                                            print(encodingError)
                                           
                                        }
        })
    }
    
    func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
    }

}


