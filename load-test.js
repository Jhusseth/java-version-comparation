import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '3m', target: 10 }, 
    { duration: '5m', target: 600 }, 
    { duration: '3m', target: 0 },
  ],
};

const services = [
  { name: 'Java17', url: 'http://localhost:8081/compute' },
  { name: 'Java21', url: 'http://localhost:8082/compute' },
  { name: 'Java25', url: 'http://localhost:8083/compute' },
];

export default function () {
  for (const svc of services) {
    let res = http.get(`${svc.url}?complexity=50000`);
    check(res, {
      [`${svc.name} status 200`]: (r) => r.status === 200,
    });
    sleep(0.1);
  }
}
