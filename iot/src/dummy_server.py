from flask import Flask, jsonify, request, make_response
import random

app = Flask(__name__)

mu = {
    'ratio': 0.2,
    'humid': 17.7,
    'temp': 21.7,
    'ec': 195,
    'ph': 6.0,
    'nitro': 17,
    'phos': 51,
    'pota': 43,
    'light': 1423,
}

@app.route('/read', methods=['GET'])
def read_data():
    data = {
        "humid": round(random.normalvariate(mu['humid'], mu['humid'] * mu['ratio']), 1) ,
        "temp": round(random.normalvariate(mu['temp'], mu['temp'] * mu['ratio']), 1),
        "ec": round(random.normalvariate(mu['ec'], mu['ec'] * mu['ratio'])),
        "ph": round(random.normalvariate(mu['ph'], mu['ph']  * mu['ratio']), 1),
        "nitro": round(random.normalvariate(mu['nitro'], mu['nitro'] * mu['ratio'])),
        "phos": round(random.normalvariate(mu['phos'], mu['phos']  * mu['ratio'])),
        "pota": round(random.normalvariate(mu['pota'], mu['pota']  * mu['ratio'])),
        "light": round(random.normalvariate(mu['light'], mu['light']  * mu['ratio']))  
    }

    columns = request.args.get('col').split('_')
    result = {}
    for col in columns:
        if col in data:
            result[col] = str(data[col])
    return jsonify(result)

@app.route('/set', methods=['GET'])
def set_mu():
    global mu

    if 'reset' in request.args and request.args['reset'] == '1':
        mu = {
            'ratio': 0.2,
            'humid': 17.7,
            'temp': 21.7,
            'ec': 195,
            'ph': 6.0,
            'nitro': 17,
            'phos': 51,
            'pota': 43,
            'light': 1423
        }
        response= make_response("All value has been reset.")
        response.headers['Content-type'] = 'text/plain'
        return response


    output = ""

    for key in mu.keys():
        if key in request.args:
            mu[key] = float(request.args[key])
            output += f'{key} changed to {mu[key]}\n'

    response= make_response(output)
    response.headers['Content-type'] = 'text/plain'
    return response


if __name__ == '__main__':
    app.run(port=8080, debug=True)
