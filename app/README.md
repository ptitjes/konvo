# Konvo Sample Application

1. Create a `data` directory. You can put character cards in the `characters` subdirectory.
2. Create a `config/konvo.json` file, similar to the following:
   ```json
   {
     "dataDirectory": "./data",
     "ollama": {
       "url": "<your-ollama-server-url>"
     },
     "discord": {
       "token": "<your-discord-application-token>"
     },
     "mcp": {
       "servers": {
         "wikipedia": {
           "transport": {
             "type": "stdio"
           },
           "process": {
             "command": [
               "java",
               "-jar",
               "../konvo-tool-web/build/libs/konvo-tool-web-all.jar",
               "--stdio"
             ]
           }
         },
         "fs": {
           "transport": {
             "type": "stdio"
           },
           "process": {
             "command": [
               "docker",
               "run",
               "-i",
               "--rm",
               "--mount",
               "type=bind,src=/<some-path>,dst=/projects,ro",
               "mcp/filesystem",
               "/projects"
             ]
           }
         }
       },
       "toolPermissions": {
         "default": "allow",
         "rules": [
           {
             "pattern": "fs:.*",
             "permission": "ask"
           }
         ]
       }
     }
   }
   ```
3. Start the application (i.e. `./gradlew :app:run`)
