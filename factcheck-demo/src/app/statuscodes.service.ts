import { Injectable } from '@angular/core';

@Injectable()
export class StatuscodesService {
  private statusCodes  = new Map<number, string>();
  constructor() {

    this.statusCodes.set(0, 'Error: Server is not connected.');
    this.statusCodes.set(100, 'Continue');
    this.statusCodes.set(101, 'Switching Protocols');
    this.statusCodes.set(200, 'OK');
    this.statusCodes.set(201, 'Created');
    this.statusCodes.set(202, 'Accepted');
    this.statusCodes.set(203, 'Non-Authoritative Information');
    this.statusCodes.set(204, 'No Content');
    this.statusCodes.set(205, 'Reset Content');
    this.statusCodes.set(206, 'Partial Content');
    this.statusCodes.set(300, 'Multiple Choices');
    this.statusCodes.set(301, 'Moved Permanently');
    this.statusCodes.set(302, 'Found');
    this.statusCodes.set(303, 'See Other');
    this.statusCodes.set(304, 'Not Modified');
    this.statusCodes.set(307, 'Temporary Redirect');
    this.statusCodes.set(308, 'Permanent Redirect');
    this.statusCodes.set(400, 'Bad Request');
    this.statusCodes.set(401, 'Unauthorized');
    this.statusCodes.set(403, 'Forbidden');
    this.statusCodes.set(404, 'Not Found: The requested resource could not be found but may be available again in the future.' +
      ' Subsequent requests by the client are permissible.');
    this.statusCodes.set(405, 'Method Not Allowed');
    this.statusCodes.set(406, 'Not Acceptable');
    this.statusCodes.set(407, 'Proxy Authentication Required');
    this.statusCodes.set(408, 'Request Timeout');
    this.statusCodes.set(409, 'Conflict');
    this.statusCodes.set(410, 'Gone');
    this.statusCodes.set(411, 'Length Required');
    this.statusCodes.set(412, 'Precondition Failed');
    this.statusCodes.set(413, 'Payload Too Large');
    this.statusCodes.set(414, 'URI Too Long');
    this.statusCodes.set(415, 'Unsupported Media Type');
    this.statusCodes.set(416, 'Range Not Satisfiable');
    this.statusCodes.set(417, 'Expectation Failed');
    this.statusCodes.set(422, 'Unprocessable Entity');
    this.statusCodes.set(426, 'Upgrade Required');
    this.statusCodes.set(428, 'Precondition Required');
    this.statusCodes.set(429, 'Too Many Requests');
    this.statusCodes.set(431, 'Request Header Fields Too Large');
    this.statusCodes.set(451, 'Unavailable For Legal Reasons');
    this.statusCodes.set(511, 'Network Authentication Required');
    this.statusCodes.set(500, 'Internal server error : ' +
      'The server encountered an unexpected condition which' +
      ' prevented it from fulfilling the request. It may be' +
      ' cause by errors in the file');
    this.statusCodes.set(501, 'Not Implemented');
    this.statusCodes.set(502, 'Bad Gateway');
    this.statusCodes.set(503, 'Service Unavailable');
    this.statusCodes.set(504, 'Gateway Timeout');
    this.statusCodes.set(505, 'HTTP Version Not Supported');
  }

  get(item: any) {
    return this.statusCodes.get(item);
  }
}
