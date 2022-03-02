# IOStreams
 progjar exercise 1

TODO
1. open a web page given an URI and show text
2. show clickable links
3. download file regardless its size
4. download file in parallel
5. follow redirection
6. show respective HTTP error messages
7. Open a web page that is protected by HTTP basic auth


1. just give it a link and input 'get'
2. automatically show the clickable links
3. auto download
4. auto download
5. auto redirect if it is being redirected
6. always show the respective response code
7. for example
```
Enter url: http://127.0.0.1/api/user/login/
You're accessing http://127.0.0.1/api/user/login/
Enter METHOD: post
Enter email: ei@gmail.com
Enter password: password

{"user":{"id":4,"name":"ei-chan","email":"ei@gmail.com","email_verified_at":null,"bio":"bio","mobile":"082141414361","city":"inazuma","created_at":"2021-10-28T13:19:10.000000Z","updated_at":"2021-12-08T10:45:32.000000Z","avatar":"avatar-4.png"},"access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIzIiwianRpIjoiNDBkNGZkMDMyNjg2ZTBlMDA3NWIyNGIwMTZhZDY0MTM5ODkxN2RiOWJmNDJhN2Q0OWNkODBmNmZkOTE1N2U5ZjY0ODUxMTYzMmUxZThkYTciLCJpYXQiOjE2NDYxOTM2NTAuMzkzODk0LCJuYmYiOjE2NDYxOTM2NTAuMzkzOTA5LCJleHAiOjE2Nzc3Mjk2NTAuMzc0MDE1LCJzdWIiOiI0Iiwic2NvcGVzIjpbXX0.lQFi4vs7j3h-CN6fpLtL1k2JqqenNJg5cEXh7oRjInpZKMpi4-H438SezFIQbj75ZJXFsFCOFytmgxUZkEFGUXy77P-5KY133dg-J_6ieg0yw3HM5w0QsxD1TKxEBYSLUd2wOTpAo_CvoV8LkbEIde0bXlHr3aBjJpYGnnZ3fORD0B1HfBgM0esW2VrsLpuaMGSAtgHX_YEEztLddluoY8iw3JjmwxvrkJitSkLFSUA2YSrpvbsSVxCkYsl4xf4Osiw7v2ZVRZAmxmFpCUUqBTVi2fc2w17h-4iWRLgN4MOM6BAsREXOeQJTv6an3oc_muPZdHd-gscoUnPNFARsJucPk54LNLPtwm0h6xm39uHVxl-2pFarTSWZXzTHwcNezDSnGOmLMeOLRT5Yvjl-q3W0UaWklyc6BbhSYDa3s7HMMyVaC-vEYsofvj9htogF96d5TE_PnICm79Eh41axeLJpN0VUp0B5LVzCAhbbzJ5rnXUTV_J_TaFDLbWqlEWoRTIMltl7kxBEkroAKgD83XDpX1BpeaTHUElZFT7VZY4QvKzQX9WmgbpgmUpmdwzcWTbBDShlc2lDSm_sb5mXDthLGGtgZvmtTUbYnCdzUWoNw-cAcE6RgUKtLBk_ib2R8pooVQTcsIGGwssr5eCtR_oWo7L05jtSJdtprL2pUoc"}
 (200 OK)
Program is downloading the file in the background

=======================================================
Enter url: 
```

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

### 301 Moved Permanently
```
classroom.its.ac.id
"GET /auth/oidc HTTP/1.1\r\nHost: ${link}\r\n\r\n"
```
```
HTTP/1.1 301 Moved Permanently
Server: nginx/1.10.3
Date: Mon, 28 Feb 2022 03:39:30 GMT
Content-Type: text/html; charset=iso-8859-1
Content-Length: 330
Connection: keep-alive
Location: http://classroom.its.ac.id/auth/oidc/
```

### 303 See Other
```
"classroom.its.ac.id", "/auth/oidc/"
```
```
HTTP/1.1 303 See Other
Server: nginx/1.10.3
Date: Mon, 28 Feb 2022 04:08:08 GMT
Content-Type: text/html; charset=UTF-8
Content-Length: 425
Connection: keep-alive
Location: https://classroom.its.ac.id
Content-Language: en
```
same request but using HTTPS
```
HTTP/1.1 303 See Other
Server: nginx/1.10.3
Date: Mon, 28 Feb 2022 04:23:00 GMT
Content-Type: text/html; charset=utf-8
Content-Length: 731
Connection: keep-alive
Set-Cookie: MoodleSession=tksihos3aap0018v6luc37nf5a; path=/; secure
Expires: Thu, 19 Nov 1981 08:52:00 GMT
Cache-Control: no-store, no-cache, must-revalidate
Pragma: no-cache
Location: https://my.its.ac.id/authorize?response_type=code&client_id=759DF3B8-B85E-46B1-9C48-CAF2AB31AE4F&scope=openid%20profile%20email&nonce=N621c4e24e3dda&response_mode=form_post&resource=https%3A%2F%2Fmy.its.ac.id%2Fuserinfo&state=tM4AutZK4KNtiE7&redirect_uri=https%3A%2F%2Fclassroom.its.ac.id%2Fauth%2Foidc%2F
Content-Language: en
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