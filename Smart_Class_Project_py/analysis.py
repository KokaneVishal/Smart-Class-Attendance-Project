import os
import time
import glob
import pandas as pd
import matplotlib.pyplot as plt
from collections import defaultdict
from tkinter import messagebox
import firebase_admin
from firebase_admin import credentials, storage

json_file = glob.glob("other/*.json")
if not json_file:
  messagebox.showwarning("Warning", "Please add Firebase 'serviceAccountKey.json'. Add to 'other/' folder")
  exit()

cred = credentials.Certificate('other/smart-class-project-firebase-adminsdk-m2ou9-89a24d289d.json')
firebase_admin.initialize_app(cred, {
    'storageBucket': 'smart-class-project.appspot.com'
})

bucket = storage.bucket()

current_time = time.strftime("%l:%M | %d-%m-%Y")
print("[ "+current_time+" ]")

folder_path = "Attendance/"
attendance_files = [file for file in os.listdir(folder_path) if file.endswith('.csv')]
attendance_data = []

attendance_count = {}
for file in attendance_files:
    df = pd.read_csv(os.path.join(folder_path, file))
    
    for index, row in df.iterrows():
        student = row['Name']  
        lecture = row['Lecture']  
        status = row['Status']  
        
        key = (lecture, student)
        if key not in attendance_count:
            attendance_count[key] = {'Present': 0, 'Absent': 0}
        
        if status == 'Present':
            attendance_count[key]['Present'] += 1
        elif status == 'Absent':
            attendance_count[key]['Absent'] += 1

attendance_percentage_data = []
for (lecture, student), counts in attendance_count.items():
    total_lectures = counts['Present'] + counts['Absent']
    attendance_percentage = (counts['Present'] / total_lectures) * 100 if total_lectures > 0 else 0
    attendance_percentage_data.append({'Lecture': lecture, 'Student': student, 'Attendance Percentage': attendance_percentage})

attendance_percentage_df = pd.DataFrame(attendance_percentage_data)

attendance_percentage_df.to_csv('Analysis/Attendance_Percentage_By_Lecture.csv', index=False)
print("1)Attendance Percentage By Lecture csv Saved")

# Upload csv to Firebase
file_path = 'Analysis/Attendance_Percentage_By_Lecture.csv'
filename = file_path.split("/")[-1]
firebase_storage_path = f'analysis/{filename}'
try:
    blob = bucket.blob(firebase_storage_path)
    blob.upload_from_filename(file_path)
    print(f'File uploaded successfully to {blob.public_url}')
except Exception as e:
    print(f'Error uploading file: {e}')


for file in attendance_files:
    df = pd.read_csv(os.path.join(folder_path, file))
    attendance_data.append(df)

lecturewise_attendance = defaultdict(lambda: defaultdict(int))

for df in attendance_data:
    for index, row in df.iterrows():
        lecture = row['Lecture']
        status = row['Status'] 
        lecturewise_attendance[lecture][status] += 1

report = defaultdict(lambda: defaultdict(int))

for lecture, attendance in lecturewise_attendance.items():
    total_present = attendance['Present']
    total_absent = attendance['Absent']
    report[lecture]['Present'] += total_present
    report[lecture]['Absent'] += total_absent

lecture_names = list(lecturewise_attendance.keys())
present_counts = [lecturewise_attendance[lecture]['Present'] for lecture in lecture_names]
absent_counts = [lecturewise_attendance[lecture]['Absent'] for lecture in lecture_names]

studentwise_attendance = defaultdict(lambda: defaultdict(int))

for lecture, attendance in lecturewise_attendance.items():
    total_present = attendance['Present']
    total_absent = attendance['Absent']
    for student in attendance_data[0]['Name']:
        studentwise_attendance[student]['Total'] += 1
        if attendance_data[0][(attendance_data[0]['Name'] == student) & (attendance_data[0]['Status'] == 'Present')].shape[0] > 0:
            studentwise_attendance[student]['Present'] += 1

student_names = list(studentwise_attendance.keys())
attendance_percentages = [(student, (studentwise_attendance[student]['Present'] / studentwise_attendance[student]['Total']) * 100) for student in student_names]
meeting_criteria_students = [(name, percentage) for name, percentage in attendance_percentages if percentage >= 75]

meeting_criteria_students.sort(key=lambda x: x[1], reverse=True)  

student_names = [student[0] for student in meeting_criteria_students]
attendance_percentages = [student[1] for student in meeting_criteria_students]

plt.figure(figsize=(10, len(meeting_criteria_students) * 0.5))  
plt.barh(student_names, attendance_percentages, color='skyblue')
plt.xlabel('Attendance Percentage')
plt.ylabel('Student Name')
plt.title('Students Meeting 75% Attendance Criteria [ ' + current_time + " ]")
plt.gca().invert_yaxis() 
plt.grid(axis='x')
plt.savefig('Analysis/meeting_criteria_students.png')
plt.show()
print("2)Students Bar Chart Saved")

plt.figure(figsize=(10, 8))
plt.bar(lecture_names, present_counts, label='Present')
plt.bar(lecture_names, absent_counts, bottom=present_counts, label='Absent')
plt.ylabel(' Attendance Count ')
plt.xlabel(' Lecture ')
plt.title('Attendance Report [ ' +current_time+" ]")
plt.xticks(rotation=30)
plt.legend()
plt.savefig('Analysis/bar_report.png')
plt.show()
print("3)Attendance Bar chart report Saved")

monthly_attendance = defaultdict(lambda: defaultdict(int))
for df in attendance_data:
    for index, row in df.iterrows():
        month = row['Date'].split('-')[1] 
        status = row['Status']
        monthly_attendance[month][status] += 1

month_labels = list(monthly_attendance.keys())
present_counts_monthly = [monthly_attendance[month]['Present'] for month in month_labels]
absent_counts_monthly = [monthly_attendance[month]['Absent'] for month in month_labels]

plt.figure(figsize=(8, 6))
plt.pie(present_counts_monthly + absent_counts_monthly, labels=month_labels + month_labels, autopct="%1.1f%%")
plt.title('Monthly Attendance Distribution [ ' +current_time+" ]")
plt.legend(loc='upper left')
plt.savefig('Analysis/attendance_report.png')
plt.show()
print("4)Monthly Attendance Pie Chart Saved")

overall_attendance = defaultdict(int)
for df in attendance_data:
    for index, row in df.iterrows():
        status = row['Status']
        overall_attendance[status] += 1

overall_labels = list(overall_attendance.keys())
overall_counts = list(overall_attendance.values())

plt.figure(figsize=(6, 6))
plt.pie(overall_counts, labels=overall_labels, autopct="%1.1f%%")
plt.title('Overall Attendance Distribution [ ' +current_time+" ]")
plt.legend(loc='upper left')
plt.savefig('Analysis/overall_attendance.png')
plt.show()
print("5)Overall Attendance Pie Chart Saved")


# Upload report images to Firebase Storage
image_folder = "Analysis/"

image_files = ["attendance_report.png", "bar_report.png", "meeting_criteria_students.png","overall_attendance.png"]

for image_file in image_files:
    local_file_path = os.path.join(image_folder, image_file)

    try:
        firebase_storage_path = f'reports/{image_file}'
        blob = bucket.blob(firebase_storage_path)
        blob.upload_from_filename(local_file_path)
        print(f'Report Images uploaded successfully: {local_file_path}')

    except Exception as e:
        print(f'Error uploading report image: {e}')


