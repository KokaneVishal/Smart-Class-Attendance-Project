import firebase_admin
from firebase_admin import credentials, storage, firestore
import cv2, os
import pandas as pd
import glob
import time
import datetime
import tkinter as tk
from tkinter import messagebox

json_file = glob.glob("other/*.json")
if not json_file:
    messagebox.showwarning("Warning", "Please add Firebase 'serviceAccountKey.json'. Add to 'other/' folder")
    exit()

# Initialize Firebase
cred = credentials.Certificate("other/smart-class-project-firebase-adminsdk-m2ou9-89a24d289d.json")
firebase_admin.initialize_app(cred, {
   'storageBucket': "smart-class-project.appspot.com"
})

db = firestore.client()
bucket = storage.bucket()
subjects = ["Select Subject", "Java", "Python", "CPP", "PHP", "JS", "DBMS", "DSA", "AI", "Account", "GK", "Management", "HR", "Marketing", "Cyber Security"]
classes = ["Select Class", "MCA", "MBA"]

window = tk.Tk()
window.title("Track Attendance")
window.configure(background="#E0F7FA")
window.geometry("1280x670")
window.resizable(0, 0)

def trackImages():
    if not os.path.exists("DataSet/Trainner.yml"):
        messagebox.showwarning("Warning", "No training data available. Capture images first.")
        return

    if not os.path.exists("other/StudentRecord.csv"):
        messagebox.showwarning("Warning", "No student data available. Capture images first.")
        return
    
    if selected_subject.get() == "Select Subject":
        messagebox.showwarning("Warning", "Please select a subject.")
        return
    
    if selected_class.get() == "Select Class":
        messagebox.showwarning("Warning", "Please select a class.")
        return

    lecture = selected_subject.get()
    class_selected = selected_class.get()

    recognizer = cv2.face.LBPHFaceRecognizer_create()
    recognizer.read("DataSet/Trainner.yml")
    harcascadePath = "other/haarcascade_frontalface_default.xml"
    faceCascade = cv2.CascadeClassifier(harcascadePath)
    df_students = pd.read_csv("other/StudentRecord.csv")

    cam = cv2.VideoCapture(0)
    font = cv2.FONT_HERSHEY_SIMPLEX

    col_names = ["Id", "Name", "Lecture", "ClassName", "Date", "Time", "Status"]
    attendance = pd.DataFrame(columns=col_names)

    # Initialize status for all students as Absent
    for _, student in df_students.iterrows():
        attendance.loc[len(attendance)] = [student["Id"], student["Name"], lecture, class_selected, "", "", "Absent"]

    # Define timeStamp here
    timeStamp = datetime.datetime.fromtimestamp(time.time()).strftime("%H:%M:%S")
    Hour, Minute, Second = timeStamp.split(":")

    # Define date outside the loop
    date = datetime.datetime.fromtimestamp(time.time()).strftime("%Y-%m-%d")
    attendance["Date"] = date
    attendance["Time"] = timeStamp 

    while True:
        ret, im = cam.read()
        gray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
        faces = faceCascade.detectMultiScale(gray, 1.2, 5)
        recognized_ids = []

        for x, y, w, h in faces:
            cv2.rectangle(im, (x, y), (x + w, y + h), (225, 0, 0), 2)
            Id, conf = recognizer.predict(gray[y : y + h, x : x + w])
            if conf < 50:
                recognized_ids.append(Id)
                ts = time.time()
                timeStamp = datetime.datetime.fromtimestamp(ts).strftime("%H:%M:%S")
                aa = df_students.loc[df_students["Id"] == Id]["Name"].values
                tt = str(Id) + "-" + aa
                status = "Present"
                attendance.loc[attendance["Id"] == Id, ["Date", "Time", "Status"]] = [date, timeStamp, status]
            else:
                Id = "Unknown"
                tt = str(Id)

            if conf > 75:
                noOfFile = len(os.listdir("UnknownImages")) + 1
                cv2.imwrite(
                    "UnknownImages/Image" + str(noOfFile) + ".jpg",
                    im[y : y + h, x : x + w],
                )
            cv2.putText(im, str(tt), (x, y + h), font, 1, (255, 255, 255), 2)
            recognized_ids.append(Id)

        cv2.imshow("Face Recognizing (Q for Quit)", im)
        pass

        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

        for _, student in df_students.iterrows():
            if student["Id"] in recognized_ids:
                status = "Present"
            else:
                status = "Absent"
            attendance.loc[attendance["Id"] == student["Id"], "Status"] = status

    fileName = "Attendance_" + date + "_" + Hour + "-" + Minute + ".csv"
    file_path = "Attendance/" + fileName
    attendance.to_csv(file_path, index=False)
  
    # Upload CSV file to Firebase Storage
    try:
        upload_folder = "attendance/upload/"
        blob = bucket.blob(upload_folder + fileName)
        blob.upload_from_filename(file_path)
        print("CSV file uploaded to Firebase Storage.")

    except Exception as e:
        print("Error uploading CSV file to Firebase Storage: ", e)
        return

    # Get download URL
    try:
        download_url = blob.generate_signed_url(expiration=datetime.timedelta(days=1), method='GET')
        doc_ref = db.collection('attendance').document(fileName)
        doc_ref.set({
            'name': fileName,
            'url': download_url
        })
        print("CSV file path saved to Cloud Firestore.")

    except Exception as e:
        print("Error: ", e)

    cam.release()
    cv2.destroyAllWindows()

    res = attendance
    message.configure(text=res)

# GUI
lecture_label = tk.Label(
    window,
    text="Select Subject:",
    width=20,
    fg="black",
    bg="#FFEB3B",
    height=2,
    font=("times", 15, "bold"),
)
lecture_label.place(x=150, y=150)

selected_subject = tk.StringVar(window)
selected_subject.set(subjects[0])
subject_dropdown = tk.OptionMenu(window, selected_subject, *subjects)
subject_dropdown.config(width=15, font=("times", 15, "bold"), bg="#FFFFFF", fg="black")
subject_dropdown.place(x=390, y=155)

class_label = tk.Label(
    window,
    text="Select Class:",
    width=20,
    fg="black",
    bg="#FFEB3B",
    height=2,
    font=("times", 15, "bold"),
)
class_label.place(x=150, y=200)

selected_class = tk.StringVar(window)
selected_class.set(classes[0])  # default value
class_dropdown = tk.OptionMenu(window, selected_class, *classes)
class_dropdown.config(width=15, font=("times", 15, "bold"), bg="#FFFFFF", fg="black")
class_dropdown.place(x=390, y=205)

lbl = tk.Label(
    window,
    text="Face Recognition Based Smart Class Attendance Project",
    bg="#FFEB3B",
    fg="black",
    width=50,
    height=2,
    font=("times", 30, "italic bold"),
)
lbl.place(x=130, y=20)

lbl1 = tk.Label(
    window,
    text="↓ List Of Students ↓",
    width=110,
    fg="black",
    bg="#B3E5FC",
    height=2,
    font=("times", 15, "bold"),
)
lbl1.place(x=120, y=300)

message = tk.Label(
    window,
    text="",
    fg="black",
    bg="#B3E5FC",
    activeforeground="green",
    width=100,
    height=12,
    font=("times", 15, "bold"),
)
message.place(x=140, y=390)

trackImg = tk.Button(
    window,
    text="(0) Track Image",
    command=trackImages,
    fg="black",
    bg="#8BC34A",
    width=20,
    height=3,
    activebackground="Green",
    font=("times", 15, "bold"),
)
trackImg.place(x=650, y=160)

quitWindow = tk.Button(
    window,
    text="Quit",
    command=window.destroy,
    fg="black",
    bg="#FF5252",
    width=20,
    height=3,
    activebackground="Red",
    font=("times", 15, "bold"),
)
quitWindow.place(x=930, y=160)

window.mainloop()
