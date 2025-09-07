structure

src/
└── main/
├── java/
│   └── com.example.app/
│       ├── config/              # Security, JWT, and DB config
│       ├── controller/          # REST endpoints
│       ├── dto/                 # Request/response payloads
│       ├── entity/              # Hibernate entities
│       ├── exception/           # Custom exceptions & handlers
│       ├── repository/          # Spring Data JPA interfaces
│       ├── security/            # JWT filters, utils, auth logic
│       ├── service/             # Business logic
│       └── AppApplication.java  # Main class
└── resources/
├── application.properties   # DB, security, and JPA config
└── schema.sql / data.sql    # Optional: DB init scripts