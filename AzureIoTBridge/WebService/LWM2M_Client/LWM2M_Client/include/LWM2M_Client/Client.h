
#include "liblwm2m.h"
#include "connection.h"
#include "lightclient.h"

#include <memory>
#include <vector>
#include <string>


class Client {

private:

  typedef struct
  {
      lwm2m_object_t* securityObjP;
      int sock;
      connection_t* connList;
      int addressFamily;
  } client_data_t;

  //Device objects:
  std::unique_ptr<lwm2m_object_t> securityObj;
  std::unique_ptr<lwm2m_object_t> serverObj;
  std::unique_ptr<lwm2m_object_t> deviceObj;
  std::unique_ptr<lwm2m_object_t> testObj;

  client_data_t data;
  std::unique_ptr<lwm2m_context_t> lwm2mH;
  std::vector<lwm2m_object_t*> objects;
  std::string name;
  const std::string localPort;
  unsigned int objectN;
  int result;
  int opt;
  unsigned int maxPacketSize;

public:

  //! Default constructor
  Client(std::string name, std::string localPort, unsigned int objectN, unsigned int maxPacketSize);

  //! Destructor
  ~Client() ;

  //! communicate()
  void communicate();
};
