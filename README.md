### Folder Structure

```
com.ambulance.dispatch_system
├── common/
│   ├── entity/
│   │   └── Hospital.java
│   └── repository/
│       └── HospitalRepository.java
├── routing/            (Task 1)
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
├── allocation/         (Task 2)
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
├── network/            (Task 3)
├── triage/             (Task 4)
├── scheduling/         (Task 5)
└── DispatchSystemApplication.java
```

### Note
- Please create relevant folders/packages per task as mentioned above.
- Create `application-local.properties` file in the root and add the DB credentials shared to access the same database we will be using.
- Create your own branches (your_name) for development.
- All updates that will be going to main branch strictly should be via **Pull Requests**, no force pushes to main.
