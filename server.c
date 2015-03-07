/*
compile :  gcc server.c -o server -lpthread `mysql_config --cflags --libs`
*/

#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<sys/socket.h>
#include<arpa/inet.h> //inet_addr
#include<unistd.h> //write
#include<pthread.h>
#include<mysql/mysql.h>

//thread function
void *connection_handler(void *);

//spilt string
int splitstr(char src[1024], char c, char dst[1024][1024]){
	char temp = src[0];
	src[strlen(src)]='\0';
	int i=0,x=0,y=0;
	while(src[i] != '\0'){
		if(src[i] != c){
			dst[x][y] = src[i];
			y++;
		}
		else if (src[i] == c){
			x++;
			y = 0;
		}
		i++;
	}
}

//print error mysql
void finish_with_error(MYSQL *con)
{
  fprintf(stderr, "%s\n", mysql_error(con));
  mysql_close(con);
  return;        
}

int main(int argc , char *argv[])
{
	int socket_desc , client_sock , c;
	struct sockaddr_in server , client;
//Create socket
	socket_desc = socket(AF_INET , SOCK_STREAM , 0);
	if (socket_desc == -1)
	{
		printf("Could not create socket");
	}
	puts("Socket created");
//Prepare the sockaddr_in structure
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons( 8888 );
//Bind
	int optval = 1;
	setsockopt(socket_desc,SOL_SOCKET, SO_REUSEPORT, &optval, sizeof(optval));
	if( bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) < 0)
	{
//print the error message
		perror("bind failed. Error");
		return 1;
	}
	puts("bind done");
	listen(socket_desc , 3);
//Accept and incoming connection
	puts("Waiting for incoming connections...");
	c = sizeof(struct sockaddr_in);
	pthread_t thread_id;
	while( (client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c)) )
	{
		puts("Connection accepted");
		if( pthread_create( &thread_id , NULL , connection_handler , (void*) &client_sock) < 0)
		{
			perror("could not create thread");
			return 1;
		}
//Now join the thread , so that we dont terminate before the thread
//pthread_join( thread_id , NULL);
		puts("Handler assigned");
	}
	if (client_sock < 0)
	{
		perror("accept failed");
		return 1;
	}
	return 0;
}



void *connection_handler(void *socket_desc){
	//Get the socket descriptor
	int sock = *(int*)socket_desc;
	int read_size;
	char client_message[2000];
	MYSQL *con = mysql_init(NULL);
	if (con == NULL) {
      fprintf(stderr, "%s\n", mysql_error(con));
      return;
  	}

  	//connection mysql db
  	if (mysql_real_connect(con, "localhost", "root", "root", "chatdb", 0, NULL, 0) == NULL) {
      finish_with_error(con);
  	}

//Receive
	while( (read_size = recv(sock , client_message , 2000 , 0)) > 0 )
	{
//end of string
		client_message[read_size] = '\0';
		//printf("%s",client_message);
		//get command
		
		char message[1024][1024];
		char command[1024];
		splitstr(client_message,' ',message);
		strcpy(command,message[0]);

		////process command
		if(!strcmp(command,"signup")){
			//cek di db ada username yang sama atau nggak	^
			//klo ada yg sama send "signup exsist"			^
			//klo nggak ada insert ke db, send "signup ok"	^

			int num_rows;
			char username[20];
			char password[30];
			char queryselect[1024];	
			char queryinsert[1024];

			strcpy(username,message[1]);
			strcpy(password,message[2]);

			//create query syntax
			strcpy(queryselect,"SELECT * FROM user where username = '");
			strcat(queryselect,username);
			strcat(queryselect,"'");

			//select
  			if (mysql_query(con, queryselect)) finish_with_error(con);
  			MYSQL_RES *result = mysql_store_result(con);
			if (result == NULL) finish_with_error(con);
  			num_rows = mysql_num_rows(result);

  			if(num_rows <= 0){
  				//create query syntax "Insert into user values('username','password',NOW());"
  				strcpy(queryinsert,"insert into user values('");
  				strcat(queryinsert,username);
  				strcat(queryinsert,"','");
  				strcat(queryinsert,password);
  				strcat(queryinsert,"',NOW());");

  				if ( mysql_query(con, queryinsert) ) finish_with_error(con);

  				send(sock , "signup ok" , strlen("signup ok"),0);
  			}
  			else send(sock , "signup exsist" , strlen("signup exsist"),0);
  			
  			memset(username, 0, 2000);
  			memset(password, 0, 2000);
  			memset(queryselect, 0, 2000);
  			memset(queryinsert, 0, 2000);
		}
		
		else if(!strcmp(command,"login")){
			//cek username n pass di db									^
			//klo ada 													^
				//send "login ok"										^
				//update seen user di db								^
				//query db message yg tujuannya ke dia, kirim semua
				//kirim list online user
			//klo nggak ada												^
				//send "login wrong"									^
									
			int num_rows;	
			char username[20];
			char password[30];
			char queryselect[1024];
			char queryupdate[1024];
			strcpy(username,message[1]);
			strcpy(password,message[2]);

			//create query syntax "select * from user where username = 'username' and passsword = 'password';"
			strcpy(queryselect,"SELECT * FROM user where username = '");
			strcat(queryselect,username);
			strcat(queryselect,"' and password = '");
			strcat(queryselect,password);
			strcat(queryselect,"';");

			//select
  			if (mysql_query(con, queryselect)) finish_with_error(con);
  			MYSQL_RES *result = mysql_store_result(con);
			if (result == NULL) finish_with_error(con);
  			num_rows = mysql_num_rows(result);

  			if(num_rows > 0) {
  				send(sock , "login ok" , strlen("login ok"),0);

  				//create query update
  				strcpy(queryupdate,"update user set lastseen = NOW() where username = '");
  				strcat(queryupdate,username);
  				strcat(queryupdate,"';");

  				if ( mysql_query(con, queryupdate) ) finish_with_error(con);
  			}
  			else{
  				send(sock , "login wrong" , strlen("login wrong"),0);
  			}

  			memset(username, 0, 2000);
  			memset(password, 0, 2000);
  			memset(queryselect, 0, 2000);
  			memset(queryupdate, 0, 2000);
		}
		
		else if(!strcmp(command,"message")){
			//get from, to, id, body
			//insert pesan ke db
			//cek online user, klo online, dapetin socketnya, kirim pesan ke tujuan
			//send "message ok"
			send(sock , "message" , strlen("message"),0);
		}
		else if(!strcmp(command,"ping")){
			//update seen user di db
			send(sock , "ping" , strlen("ping"),0);
		}
		else if(!strcmp(command,"delivered_message")){
			//dapetin id message
			//query db dg id message, dapetin pengirimnya
			//buat pesan ("delivered_message <id>") dg pk d_<id> tujuan ke pengirim dari server, insert ke db
			//cek online user, klo online, dapetin socketnya, kirim pesan
			send(sock , "delivered_message" , strlen("delivered_message"),0);
		}
		else if(!strcmp(command,"delivered_info")){
			//dapetin id
			//hapus pesan di db dengan pk d_<id>
			send(sock , "delivered_info" , strlen("delivered_info"),0);
		}
		else{
			send(sock , "unknown" , strlen("unknown"),0);
		}
		//*/

//Send
		//send(sock , client_message , strlen(client_message),0);
//clear the message buffer
		memset(command, 0, 2000);
		memset(client_message, 0, 2000);
		memset(message, 0, 2000);
	}
	if(read_size == 0)
	{
		//update seen user
		puts("Client disconnected");
		fflush(stdout);
	}
	else if(read_size == -1)
	{
		perror("recv failed");
	}
	return 0;
} 
