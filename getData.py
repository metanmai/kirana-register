import random
from time import sleep
from datetime import datetime
import requests as rq
import json

def randomDate():
    year = random.randint(2015, 2020)
    month = random.randint(1, 12)
    day = random.randint(1, 28)
    hour = random.randint(0, 23)
    minute = random.randint(0, 59)
    second = random.randint(0, 59)
    date = datetime(year, month, day, hour, minute, second)
    return date.strftime('%Y-%m-%dT%H:%M:%SZ')

def postData():
    url = "http://localhost:5000/api/v1/transactions"
    amount = round(random.uniform(1, 10000), 2)
    date = randomDate()
    paymentMethod = random.choice(["Credit Card", "Debit Card", "Cash", "Net Banking", "UPI"])
    customerId = random.randint(100000, 1000000)
    currency = random.choice(["INR", "GBP", "USD", "EUR", "AUD", "CAD", "JPY", "CNY", "CHF", "NZD"])

    data = {
        "amount": amount,
        "date": {"$date": date},
        "paymentMethod": paymentMethod,
        "customerId": customerId,
        "currency": currency
    }

    print(json.dumps(data))

    try:
        response = rq.post('http://localhost:8080/transact', json=json.dumps(data))
        sleep(5)
        print(response)
        print('------------------------------------')
    
    except Exception as e:
        print('Failed', e)
        return

# for i in range(10000):
postData()