#include <iostream>
#include <string>

#include <LWM2M_Client/Client.h>


int main(int argc, char* argv[]){
  std::cout << argc << " " << argv[0];

  std::string name = "test_client";
  std::string localPort = "56830";
  unsigned int objectN = 4;
  unsigned int maxPacketSize = 2048;

  Client client(name, localPort, objectN, maxPacketSize);

  client.communicate();
  return 0;
}
