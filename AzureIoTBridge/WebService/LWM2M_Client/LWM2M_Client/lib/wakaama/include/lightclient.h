#ifndef LIGHTCLIENT_H
#define LIGHTCLIENT_H

#ifdef __cplusplus
extern "C" {
#endif

lwm2m_object_t* get_security_object();
lwm2m_object_t* get_server_object();
lwm2m_object_t* get_object_device();
lwm2m_object_t* get_test_object();

//Specific light client functions:
void print_state(lwm2m_context_t * lwm2mH);


#ifdef __cplusplus
}
#endif


#endif
