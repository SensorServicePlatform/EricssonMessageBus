import urllib2
from random import random, randint
from workflowSetup import avgTempPrepare, virtualDevicePrepare, subscribe
import time

def run(peerList, totalRounds):#roomTemp1, roomTemp2, bodyTemp, bloodPressure, heartRate):
  currentRound = 0
  while currentRound < totalRounds:
    for peer in peerList:
    # peer = {name : xxx, topic: xxx, metadata: [{field: xxx, type: xxx, range: (x,x), value: xxx}]}
    #roomTemp1 publishes rommTemp1
      payload = "topic="+peer['topic']
      metadata = []
      for entry in peer['metadata']:
        if entry['type'] == 'String':
          metadata.append(entry['field'] + ':' + entry['value'])
        elif entry['type'] == 'Integer':
          metadata.append(entry['field'] + ':' + str(randint(entry['range'][0], entry['range'][1])))
        elif entry['type'] == 'Float':
          metadata.append(entry['field'] + ':' + str(random() * (entry['range'][1] - entry['range'][0]) + entry['range'][0]))
      payload += '&' + 'metadata=' + '|'.join(metadata)
      try:
        print payload
        response = urllib2.urlopen("http://"+ peer['name'] + ".herokuapp.com/publish?" + payload) 
        print peer['name'] + " : " + response.read()
      except urllib2.URLError as e:
        print peer['name'] + "fails to publish: "+ peer['topic'] + e.reason
    currentRound += 1
    print 'Round ' + str(currentRound) + " done"
      #avoid wait time when totalRound eqauls to 1
    if currentRound == totalRounds:
      break
    else:
      time.sleep(5)

def workflow(peerAvg, peerDevice, peerList, totalRounds):
  for peer in peerList:
    subscribe(peer['name'], 'Interval')
  avgTempPrepare(peerAvg)
  virtualDevicePrepare(peerDevice)
  run(peerList, totalRounds)
  print "done"

if __name__ == "__main__":
  peerList = [{'name': 'message-peer-room-temperature1', 'topic': 'Temperature', 'metadata': [{'field': 'id', 'type': 'String', 'value': 'temp_sensor1'}, {'field': 'temp', 'type': 'Float', 'range': (-10, 50)}]} \
             ,{'name': 'message-peer-room-temperature2', 'topic': 'Temperature', 'metadata': [{'field': 'id', 'type': 'String', 'value': 'temp_sensor2'}, {'field': 'temp', 'type': 'Float', 'range': (-10, 50)}]} \
             ,{'name': 'message-peer-heart-rate', 'topic': 'HeartRate', 'metadata': [{'field': 'id', 'type': 'String', 'value': 'heart_rate_sensor'}, {'field': 'rate', 'type': 'Integer', 'range': (0, 300)}]} \
             ,{'name': 'message-peer-blood-pressure', 'topic': 'BloodPressure', 'metadata': [{'field': 'id', 'type': 'String', 'value': 'blood_pressure_sensor'}, {'field': 'pressure', 'type': 'Integer', 'range': (0, 300)}]} \
             ,{'name': 'message-peer-body-temperature', 'topic': 'BodyTemperature', 'metadata': [{'field': 'id', 'type': 'String', 'value': 'body_temp_sensor'}, {'field': 'temp', 'type': 'Float', 'range': (-10, 50)}]} \
             ]
  subscribe('message-peer-listener', 'Interval')
  workflow('message-peer-virtual-sensor1', 'message-peer-virtual-device1', peerList, 1)
