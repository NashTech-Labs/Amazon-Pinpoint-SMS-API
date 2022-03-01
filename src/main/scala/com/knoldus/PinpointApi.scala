package com.knoldus

import software.amazon.awssdk.services.pinpoint.PinpointClient
import software.amazon.awssdk.services.pinpoint.model.{AddressConfiguration, BadRequestException, ChannelType, DirectMessageConfiguration, MessageRequest, PinpointException, SMSMessage, SendMessagesRequest}

import java.util

class PinpointApi {
  //method for sending sms
  def send(pinpoint: PinpointClient, message: String, appId: String, originationNumber: String, destinationNumber: String)
  : Unit = {
    try {
      val addConfig = AddressConfiguration.builder().channelType(ChannelType.SMS).build()

      val addressMap: java.util.Map[String, AddressConfiguration] =
        new util.HashMap[String, AddressConfiguration]()

      addressMap.put(originationNumber, addConfig)

      val smsMessage = SMSMessage.builder().body(message).messageType("TRANSACTIONAL")
        .originationNumber(originationNumber)
        .senderId(appId)
        .build()

      val direct = DirectMessageConfiguration.builder.smsMessage(smsMessage).build

      val msgReq = MessageRequest.builder()
        .addresses(addressMap)
        .messageConfiguration(direct)
        .build()

      val request = SendMessagesRequest.builder()
        .applicationId(appId)
        .messageRequest(msgReq)
        .build()
      val sendMsgResponse = pinpoint.sendMessages(request)

      val msgResponse = sendMsgResponse.messageResponse()

      val test = msgResponse.result().get(originationNumber)

      if (test.statusCode() == 200) {
        println("Message sent successfully")
      }
      else {
        throw BadRequestException.builder().build()
      }
    }
    catch {
      case e: BadRequestException =>
        println("Error: "+e.awsErrorDetails().toString)
      case ex: PinpointException =>
        println("Error : "+ex.awsErrorDetails().toString)
    }
    finally {
      pinpoint.close()
    }
  }

}
