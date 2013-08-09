import urllib2

def subscribe(peer, topic):
  #Listener subscribes peerAvg
  try:
    response = urllib2.urlopen("http://" + peer + ".herokuapp.com/subscribe?topic=" + topic) 
    print peer + " subscribes " + topic + ": " + response.read()
  except urllib2.URLError as e:
    print peer + " fails to subscribe " + topic + " : " + e.reason
  
def avgTempPrepare(peerAvg):
  #peerAvg subscribes to Temperature
  subscribe(peerAvg, "Temperature")
  subscribe('message-peer-listener', "Temperature")
  subscribe('message-peer-listener', "AvgTemperature")

def virtualDevicePrepare(peerDevice, topicList = ["BodyTemperature", "BloodPressure", "HeartRate", "AvgTemperature"]):
  #peerSum subscribes peer1 and peer2
  for topic in topicList:
    subscribe(peerDevice, topic)
    subscribe('message-peer-listener', topic)
  subscribe('message-peer-listener', "Environment")
