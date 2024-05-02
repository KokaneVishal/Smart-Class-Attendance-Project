import tkinter as tk
from tkinter import messagebox
import cv2, os
import csv
import glob
import numpy as np
import pandas as pd
from PIL import Image
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

json_file = glob.glob("other/*.json")
if not json_file:
  messagebox.showwarning("Warning", "Please add Firebase 'serviceAccountKey.json'. Add to 'other/' folder")
  exit()

cred = credentials.Certificate("other/smart-class-project-firebase-adminsdk-m2ou9-89a24d289d.json")
firebase_admin.initialize_app(cred)

db = firestore.client()
classes = ["Select Class","MCA", "MBA"]

window = tk.Tk()
window.title("Train(add)")
window.configure(background="black")
window.geometry("1280x670")
window.resizable(0, 0)

# Function to save user-details to FStore
def save_user_details(user_rollNo, user_fullName, user_email, user_class):
    user_data = {
        u'rollNo': user_rollNo,
        u'fullName': user_fullName,
        u'email': user_email,
        u'userClass': user_class
    }

    try:
        db.collection(u'users').document(user_email).set(user_data)
        print("User details saved to FStore.")
    except Exception as e:
        print("Error while saving to FStore:", e)

def takeImages():
    if not os.path.exists("other/haarcascade_frontalface_default.xml"):
        messagebox.showwarning("Warning", "Please download 'haarcascade_frontalface_default.xml'. And add to 'other/' folder")
        return

    if txt1.get() == "" or txt2.get() == "" or txt3.get() == "":
        messagebox.showwarning("Warning", "Please Enter Id, Name, Email and Class first.")
        return

    if selected_class.get() == "Select Class":
        messagebox.showwarning("Warning", "Please select a class.")
        return
    
    Id = txt1.get()
    name = txt2.get()
    email = txt3.get()
    userClass = selected_class.get()

    if isNumber(Id) and name.isalpha():
        cam = cv2.VideoCapture(0)
        harcascadePath = "other/haarcascade_frontalface_default.xml"
        detector = cv2.CascadeClassifier(harcascadePath)
        sampleNum = 0
        while True:
            ret, img = cam.read()
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            faces = detector.detectMultiScale(gray, 1.3, 5)
            for x, y, w, h in faces:
                cv2.rectangle(img, (x, y), (x + w, y + h), (255, 0, 0), 2)
                sampleNum = sampleNum + 1
                cv2.imwrite(
                    "SampleImages/ " + name + "." + Id + "." + str(sampleNum) + ".jpg",
                    gray[y : y + h, x : x + w],
                )
                cv2.imshow("Face Detecting(Q-for-Quit)", img)
            if cv2.waitKey(100) & 0xFF == ord("q"):
                break
            elif sampleNum > 100:
                break
        cam.release()
        cv2.destroyAllWindows()

        col_names = ['Id', 'Name', 'Email', 'Class']
        file_path = 'other/StudentRecord.csv'
        if os.path.exists(file_path):
            existing_df = pd.read_csv(file_path)
            if set(existing_df.columns) == set(col_names):
                print("(StudentRecord.csv) exists with correct columns")
            else:
                print("Columns don't match")
                  # Add missing columns
                for col in col_names:
                    if col not in existing_df.columns:
                        existing_df[col] = None  # You can initialize with a default value if needed
        
                # Remove extra columns
                for col in existing_df.columns:
                    if col not in col_names:
                        existing_df.drop(columns=col, inplace=True)
        
                # Save the modified DataFrame back to the CSV file
                existing_df.to_csv(file_path, index=False)
                print("(StudentRecord.csv) modified successfully.")
        else:
            df = pd.DataFrame(columns=col_names)
            df.to_csv(file_path, index=False)

        res = " Images Saved and Data uploaded to FStore."
        messagebox.showinfo("Result", res)
        row = [Id, name, email, userClass]
        with open(file_path, "a+") as csvFile:
            writer = csv.writer(csvFile)
            writer.writerow(row)
        csvFile.close()
        message.configure(text=res)

        save_user_details(Id, name, email, userClass)
        message.configure(text=res)       
    else:
        if isNumber(name):
            res = "Enter Alphabetical Name"
            message.configure(text=res)
        if Id.isalpha():
            res = "Enter Numeric Id"
            message.configure(text=res)

def getImagesAndLabels(path):
    imagePaths = [os.path.join(path, f) for f in os.listdir(path)]
    faces = []
    Ids = []
    for imagePath in imagePaths:
        pilImage = Image.open(imagePath).convert("L")
        imageNp = np.array(pilImage, "uint8")
        Id = int(os.path.split(imagePath)[-1].split(".")[1])
        faces.append(imageNp)
        Ids.append(Id)
    return faces, Ids


def trainImages():
    txt3.delete(0, "end")
    if txt1.get() == "" or txt2.get() == "":
        messagebox.showwarning("Warning", "Please Enter Id and Name first.")
        return
    recognizer = cv2.face.LBPHFaceRecognizer_create()
    harcascadePath = "other/haarcascade_frontalface_default.xml"
    detector = cv2.CascadeClassifier(harcascadePath)
    faces, Id = getImagesAndLabels("SampleImages")

    if not faces or not Id:
        messagebox.showwarning(
            "Warning", "No training data available. Capture images first."
        )
        return

    recognizer.train(faces, np.array(Id))
    recognizer.save("DataSet/Trainner.yml")
    messagebox.showwarning("Success", "Image Trained")
    res = "Ok! Image Trained"
    message.configure(text=res)
    print("Image Trained")



# GUI
lbl = tk.Label(
    window,
    text="Face Recognition Based Smart Class Attendance Project",
    bg="white",
    fg="black",
    width=50,
    height=2,
    font=("times", 30, "italic bold"),
)
lbl.place(x=120, y=20)

lbl1 = tk.Label(
    window,
    text="Enter ID/Roll No :",
    width=20,
    height=2,
    fg="black",
    bg="white",
    font=("times", 15, " bold "),
)
lbl1.place(x=260, y=250)

txt1 = tk.Entry(window, width=20, bg="white", fg="black", font=("times", 17, " bold "))
txt1.insert(0, "")
txt1.place(x=510, y=255)

lbl2 = tk.Label(
    window,
    text="Enter Name :",
    width=20,
    fg="black",
    bg="white",
    height=2,
    font=("times", 15, " bold "),
)
lbl2.place(x=260, y=310)

txt2 = tk.Entry(window, width=20, bg="white", fg="black", font=("times", 17, " bold "))
txt2.insert(0, "")
txt2.place(x=510, y=315)

lbl3 = tk.Label(
    window,
    text="Enter Email :",
    width=20,
    fg="black",
    bg="white",
    height=2,
    font=("times", 15, " bold "),
)
lbl3.place(x=260, y=370)

txt3 = tk.Entry(window, width=20, bg="white", fg="black", font=("times", 17, " bold "))
txt3.insert(0, "")
txt3.place(x=510, y=375)

lbl4 = tk.Label(
    window,
    text="Enter Class :",
    width=20,
    fg="black",
    bg="white",
    height=2,
    font=("times", 15, " bold "),
)
lbl4.place(x=260, y=430)

selected_class = tk.StringVar(window)
selected_class.set(classes[0])  # default value
class_dropdown = tk.OptionMenu(window, selected_class, *classes)
class_dropdown.config(width=15, font=("times", 15, " bold "))
class_dropdown.place(x=510, y=435)

lbl5 = tk.Label(
    window,
    text="Notification =>",
    width=20,
    fg="black",
    bg="#FFD0C5",
    height=2,
    font=("times", 15, " bold "),
)
lbl5.place(x=210, y=150)

message = tk.Label(
    window,
    text="",
    bg="#FFD0C5",
    fg="black",
    width=55,
    height=2,
    font=("times", 15, " bold "),
)
message.place(x=490, y=150)
    

def cleaFields():
    txt1.delete(0, "end")
    txt2.delete(0, "end")
    txt3.delete(0, "end")


def isNumber(s):
    try:
        float(s)
        return True
    except ValueError:
        pass


clearButton2 = tk.Button(
    window,
    text="Clear",
    command=cleaFields,
    fg="black",
    bg="white",
    width=15,
    height=1,
    activebackground="Red",
    font=("times", 15, " bold "),
)
clearButton2.place(x=780, y=315)

takeImg = tk.Button(
    window,
    text="(1)Take Images",
    command=takeImages,
    fg="black",
    bg="white",
    width=20,
    height=3,
    activebackground="Green",
    font=("times", 15, " bold "),
)
takeImg.place(x=200, y=530)

trainImg = tk.Button(
    window,
    text="(2)Train Images",
    command=trainImages,
    fg="black",
    bg="white",
    width=20,
    height=3,
    activebackground="Yellow",
    font=("times", 15, " bold "),
)
trainImg.place(x=500, y=530)

quitWindow = tk.Button(
    window,
    text="Quit:/",
    command=window.destroy,
    fg="black",
    bg="white",
    width=20,
    height=3,
    activebackground="Red",
    font=("times", 15, " bold "),
)
quitWindow.place(x=800, y=530)

window.mainloop()

