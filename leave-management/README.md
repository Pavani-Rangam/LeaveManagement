# TeamHub HR + Leave Management (Spring Boot + Thymeleaf + MySQL)

## Run in STS
1. Start MySQL. Edit `spring.datasource.username/password` in `src/main/resources/application.properties`.
2. If you ran an earlier version of this project, drop its database first: `DROP DATABASE leavedb;`
3. File > Import > Maven > Existing Maven Projects > select the `leave-management` folder.
4. Right-click project > Run As > Spring Boot App.
5. Open http://localhost:8080 and click "Create an account".

There are no demo users. Register as **Employee**, or as **Admin** using the key in
`app.admin-key` (application.properties, default `ADMIN@123` - change it).

## Modules (all sidebar items work)
Dashboard, Inbox (notifications), Calendar, Employees, Attendance (check in/out),
Performance (admin reviews), Payroll (admin payslips), Leave Management, Apply Leave, Recruitment.
Top right: Settings (profile + change password) and Notifications bell.

## Deploy
Needs a hosted MySQL. Deploy with the Dockerfile and set env vars:
SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, APP_ADMIN_KEY
