#include <iostream>
#include <cstring>
#include <cstdlib>

#include"LWM2M_Client/Client.h"

Client::Client(std::string name, std::string localPort, unsigned int objectN,
              unsigned int maxPacketSize) :
name(name), localPort(localPort), objectN(objectN), maxPacketSize(maxPacketSize){

    this->data = { 0, 0, 0, 0 };

    //Bind to port and open socket:
    this->data.addressFamily = AF_INET6;
    std::cout << "Trying to bin LWM2M client to port: " << localPort << std::endl;
    this->data.sock = create_socket(this->localPort.c_str(),
                                    this->data.addressFamily);
    if (data.sock < 0)
    {
        std::cerr << "Failed to open socket: " << errno << " " << strerror(errno) << std::endl;
        exit(EXIT_FAILURE);
    }

    //Create device objects which are in charge of communication:
    //1. Security Object:
    securityObj.reset(get_security_object());
    if(securityObj.get() == nullptr){
      std::cout << "Failed to create security object!" << std::endl;
      exit(EXIT_FAILURE);
    }

    this->data.securityObjP = securityObj.get();

    //2. Server Object:
    serverObj.reset(get_server_object());
    if(serverObj.get() == nullptr){
      std::cout << "Failed to create server object!" << std::endl;
      exit(EXIT_FAILURE);
    }


    //3. Device Object
    deviceObj.reset(get_object_device());
    if(deviceObj.get() == nullptr){
      std::cout << "Failed to create device object" << std::endl;
      exit(EXIT_FAILURE);
    }


    //4. Test Object:
    testObj.reset(get_test_object());
    if(testObj.get() == nullptr){
      std::cout << "Failed to create test object!" << std::endl;
    }


    //Initialize lwm2m context
    lwm2mH.reset(lwm2m_init(&this->data));
    if(lwm2mH.get() == nullptr){
      std::cout << "lwm2m_init() failed!" << std::endl;
      exit(EXIT_FAILURE);
    }

    //Configure the liblwm2m library with the name of the client - which shall be
    //unique for each client -, the number of objects we will be passing through
    //and the objects

    lwm2m_object_t* objects[] = {securityObj.get(),
                                 serverObj.get(),
                                 deviceObj.get(),
                                 testObj.get()};

    this->result = lwm2m_configure(lwm2mH.get(), name.c_str(), NULL, NULL, objectN, objects);
    if (this->result != 0)
    {
        std::cout << "lwm2m_configure() failed: " << result << std::endl;
    }


    std::cout << "LWM2M client \"" << name << "\" started on port: " << localPort << std::endl;

}

Client::~Client(){
    lwm2m_close(lwm2mH.get());
    close(data.sock);
    connection_free(data.connList);
}

void Client::communicate(){
  while(1){
    struct timeval tv;
    fd_set readfds;

    tv.tv_sec = 60;
    tv.tv_usec = 0;

    FD_ZERO(&readfds);
    FD_SET(data.sock, &readfds);

    print_state(lwm2mH.get());

    /*
     * This function does two things:
     *  - first it does the work needed by liblwm2m (eg. (re)sending some packets).
     *  - Secondly it adjusts the timeout value (default 60s) depending on the state of the transaction
     *    (eg. retransmission) and the time before the next operation
     */
    this->result = lwm2m_step(lwm2mH.get(), &(tv.tv_sec));
    if (this->result != 0)
    {
        std::cout << "lwm2m_step() failed: " << result << std::endl;
        exit(EXIT_FAILURE);
    }


    // Wait for an event on the socket until "tv" timed out (set
    // with the precedent function)
    this->result = select(FD_SETSIZE, &readfds, NULL, NULL, &tv);

    if (this->result < 0)
    {
        if (errno != EINTR)
        {
          std::cout << "Error in select(): " << errno << ", " << std::strerror(errno) << std::endl;
        }
    }
    else if(this->result > 0)
    {
      uint8_t buffer[this->maxPacketSize];
      int numBytes;

        //If an event happens on the socket
        if (FD_ISSET(data.sock, &readfds)){
           struct sockaddr_storage addr;
           socklen_t addrLen;

           addrLen = sizeof(addr);


          //Retrieve received data
           numBytes = recvfrom(data.sock, buffer, this->maxPacketSize, 0, (struct sockaddr *)&addr, &addrLen);
           if (0 > numBytes)
           {
              std::cout << "Error in recvfrom(): " << errno << ", " << std::strerror(errno) << std::endl;
           }
           else if (numBytes >= this->maxPacketSize)
           {
              std::cout << "Received packet is greater than maximum packet size!" << std::endl;
           }
           else if(0 < numBytes){
             connection_t * connP;
             connP = connection_find(data.connList, &addr, addrLen);
             if (connP != NULL)
             {
                //Let liblwm2m respond to the query depending on the context
                lwm2m_handle_packet(lwm2mH.get(), buffer, numBytes, connP);
             }else{
               //Packet from unknown peer
               std::cout << "Received bytes ignored." << std::endl;
             }
           }
        }
    }

  }
}
