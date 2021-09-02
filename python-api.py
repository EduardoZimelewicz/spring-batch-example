from flask import Flask, json, jsonify, request

api = Flask(__name__)


@api.route('/batch', methods=['POST'])
def post_batch():
    print(request.json)
    return jsonify(body='batch processed')


if __name__ == '__main__':
    api.run()
