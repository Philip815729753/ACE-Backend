# SWEN90017_ACE_Backend


## Introduction
This project uses Docker to containerize the application, making it easier to manage dependencies and run the application in a consistent environment.

## Installing Docker

### macOS

1. **Download Docker Desktop for Mac:**
   - Go to the [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop) page.
   - Click on "Download for Mac".

2. **Install Docker Desktop:**
   - Open the downloaded `.dmg` file.
   - Drag the Docker icon to the Applications folder.


### Windows

1. **Download Docker Desktop for Windows:**
   - Go to the [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop) page.
   - Click on "Download for Windows".

2. **Install Docker Desktop:**
   - Run the downloaded installer.
   - Follow the installation instructions.

3. **Start Docker Desktop:**
   - Open Docker Desktop from the Start menu.
   - Follow the on-screen instructions to complete the setup.

4. **Verify Installation:**
   - Open Terminal.
   - Run the command: `docker --version`
   - You should see the Docker version information.





5. **Run the MySQL Docker Container:**
   - Open Command Prompt, PowerShell, or Terminal.
   - Navigate to the `data_migration` folder :
     ```sh
     cd springboot_backend/data_migration
     ```
   - Run the following command to build docker image:
     ```sh
     docker build -t my-mysql-image .
     ```
     
   - Create the Docker network:
     ```sh
     docker network create my-network
     ```
     
   - Run the Docker container for database on my-network:
     ```sh
     docker run --name my-mysql-container --network my-network -e MYSQL_ROOT_PASSWORD=1234 -e MYSQL_DATABASE=ace -p 3307:3306 -d my-mysql-image:latest
     ```
     
6. **Run Spring Boot Backend on Docker Container(Optional):**
    - Run the following command to pull docker image from dockerHub:
     ```sh
     docker pull philipzz13498646/ace_backend:latest
     ```
    - Run the docker container for backend:
     ```sh
     docker run --name ace_backend --network my-network -e DB_URL=jdbc:mysql://my-mysql-container:3306/ace -e DB_USERNAME=root -e DB_PASSWORD=1234 -p 8080:8080 -d philipzz13498646/ace_backend:latest
     ```





### Explanation 

- **docker build -t my-mysql-image .:** This command builds a Docker image from the Dockerfile in the current directory and tags it as my-mysql-image.
- **docker run -d -p 3307:3306 --name my-mysql-container my-mysql-image:** This command runs a container from the my-mysql-image image, maps port 3306 of the container to port 3307 on the host, and names the container my-mysql-container.
- **MYSQL_ROOT_PASSWORD and MYSQL_DATABASE:**  MYSQL_ROOT_PASSWORD and MYSQL_DATABASE are both defined in the Dockerfile, and you can customize them by yourself.


