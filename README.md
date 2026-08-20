### Folder Structure

```
com.ambulance.dispatch_system
├── common/
│   ├── entity/
│   └── repository/
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
├── optimization/       (Task 5 — scheduling: fitness/, ga/, greedy/, model/)
└── DispatchSystemApplication.java
```

### Note
- Please create relevant folders/packages per task as mentioned above.
- Create `application-local.properties` file in `\src\main\resources\application-local.properties` and add the DB credentials shared to access the same database we will be using.
- Branch naming convention: `feature/taskNo/submodule`.
- All updates that will be going to main branch strictly should be via **Pull Requests**, no force pushes to main.
