# IOStreams
 progjar exercise 1

# example http response's header
### 200 OK
```
"GET /index.php/berita/lihatBerita HTTP/1.1\r\nHost: ${link}\r\n\r\n"
```
```
HTTP/1.1 200 OK
Date: Sun, 27 Feb 2022 03:44:18 GMT
Server: Apache
Set-Cookie: ci_session=VkyrHFrZvDkpm%2FDgGFntQ69Ej10gh90YnBZKsQCjyJ%2FFFWACS7eK7ys9mDK2GNmBsgMR0VrlCZEKVQG1ZaRhnqhOacXvLSQ8knSO%2F6t5LufZZEQ0op3sbSQuyU3N5lld1pM5Xh91npA6uRCbMQDF7d6RYo8MoasewI%2FC98iNO6QFcoo5jbhHXf9GW3cEQZcqT4v%2BZYBwvGv8AIXhqFX9NrZKflgpTQUtFRxs9GrYTUFWnjHsnnJ6h8TGS7IyAf1GEH334Hee56uc2slsD8koYsOrNQruPRQXQA6F%2Fb8Ha0SFQcMdjJy%2B0X3TJjRZb4TP4BQRyC%2FmN0mPIYaA68fqNZ7Y%2FHk3fXTqCeNzkxHoUZL6SWYrd3OA9ZXejaCKNGJufbs0KZr5C3%2BPjhjvse1U%2FBqmbSJchRDXjdA2vl7FIMwipu6ttQWVt574Mi9tt3Wrk7K88ubBGXHreXSufei1xpimqjQ3UI4M5EwrOh1PJVGWGLYYjGQdV4ukSR2FJicq; expires=Sun, 27-Feb-2022 05:44:18 GMT; Max-Age=7200; path=/
Vary: Accept-Encoding
Transfer-Encoding: chunked
Content-Type: text/html
```

### 400 Bad Request
```
hello world\r\n
```
```
HTTP/1.1 400 Bad Request
Date: Sun, 27 Feb 2022 05:37:45 GMT
Server: Apache
Content-Length: 226
Connection: close
Content-Type: text/html; charset=iso-8859-1
```