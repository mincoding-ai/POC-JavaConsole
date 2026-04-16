### 실행방법
powershell 에서 실행함
- julu java sdk 17 설치 필요

```
.\gradlew build
java -jar .\build\libs\untitled-1.0-SNAPSHOT.jar
```

### 동작 화면
- UP, Down 방향키로 동작됨
<img width="758" height="533" alt="image" src="https://github.com/user-attachments/assets/7809b78c-bce0-4c57-9d2e-a5c0566d6c11" />
<img width="1007" height="844" alt="image" src="https://github.com/user-attachments/assets/7be0f9f4-c1bd-4499-a1c5-adf574f5950c" />

### 문제점
- E2E 테스트가 되려면 1, 2, 3, 4 ... Scanner로 선택되고 출력되어야함
- 따라서 바뀌어야함
