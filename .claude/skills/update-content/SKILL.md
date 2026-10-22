---
name: update-content
description: content/ (경력·사례·프로필 JSON)를 고친 뒤 임베딩 재생성 → 테스트 → 커밋·푸시 → 배포 확인까지 마무리한다. 사이트 문구·사례·경력을 추가/수정/삭제했을 때 사용.
---

# 콘텐츠 수정 마무리

순서대로 진행하고, 한 단계라도 실패하면 멈추고 사용자에게 알린다.

## 1. 근거 확인
- 바꾼 문장마다 근거(코드·서류·사용자 발언)가 있는지 다시 본다. 없으면 사용자에게 묻는다.
- 새 외부 링크가 있으면 `backend/src/test/java/dev/portfolio/content/ContentGuardTest.java` 허용 목록에 넣는다.
- 항목을 지웠다면 그 항목만 보던 테스트도 같이 정리한다.

## 2. 임베딩 재생성 (Gemini 호출)
사용자 키를 레지스트리에서 읽어 명시적으로 넘긴다. 훅이 끝 4자리를 검사한다.

```bash
cd /c/Users/USER/git/portfolio && K=$(powershell -NoProfile -Command "[Environment]::GetEnvironmentVariable('GEMINI_API_KEY','User')" | tr -d '\r\n'); echo "키 끝 4자리: ${K: -4}"; GEMINI_API_KEY="$K" MSYS_NO_PATHCONV=1 docker compose -f docker-compose.dev.yml run --rm --no-deps -e GEMINI_API_KEY backend sh -c './gradlew embed --no-daemon -q > /tmp/e.log 2>&1; echo "embed exit $?"; tail -1 /tmp/e.log'
```
기대: `embed exit 0`, `임베딩 완료: 문서 N개`.

## 3. 테스트 (키 없이)
```bash
cd /c/Users/USER/git/portfolio && MSYS_NO_PATHCONV=1 docker compose -f docker-compose.dev.yml run --rm --no-deps backend sh -c 'unset GEMINI_API_KEY; ./gradlew test --rerun --no-daemon -q > /dev/null 2>&1; echo "backend exit $?"' && MSYS_NO_PATHCONV=1 docker compose -f docker-compose.dev.yml run --rm --no-deps frontend sh -c 'npx vitest run > /tmp/v.log 2>&1; echo "frontend exit $?"; grep -E "Tests " /tmp/v.log'
```
기대: 둘 다 exit 0.

## 4. 커밋·푸시
```bash
cd /c/Users/USER/git/portfolio && git add content backend frontend && git -c user.name=jsjeong -c user.email=jiseok1378@gmail.com commit -q -m "content: <무엇을 바꿨는지>" && git branch -f main HEAD && git push -q origin main && git log --oneline -1
```

## 5. 배포 확인
- GitHub Actions 실행이 성공했는지 본다.
- Render 서비스 `srv-dbcpujjrjlhs73eagimg` 의 최신 배포가 `live` 인지 Render MCP `list_deploys` 로 본다.
- 결과를 사용자에게 한 줄씩 보고한다. 확인하지 못한 단계는 확인하지 못했다고 쓴다.
