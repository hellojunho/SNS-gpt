import React, { useEffect, useMemo, useState } from 'react';

const API_BASE = 'http://localhost:8080/api';

const tabs = [
  { key: 'BLOOMBERG', label: 'Bloomberg' },
  { key: 'INVESTING', label: 'Investing.com' },
  { key: 'GPT10', label: 'GPT10' },
  { key: 'CHAT', label: 'Chat with GPT' },
  { key: 'MYPAGE', label: '마이페이지' }
];

const formatDate = (value) => {
  if (!value) return '-';
  return new Date(value).toLocaleString('ko-KR');
};

const NewsList = ({ items, onSelect, activeId }) => (
  <div className="news-list">
    {items.map((item) => (
      <button
        key={item.id}
        type="button"
        className={activeId === item.id ? 'news-item active' : 'news-item'}
        onClick={() => onSelect(item)}
      >
        <div className="news-item-title">{item.title}</div>
        <div className="news-item-meta">
          <span>{item.source}</span>
          <span>{formatDate(item.fetchedAt)}</span>
        </div>
      </button>
    ))}
    {items.length === 0 && <div className="empty">데이터가 없습니다.</div>}
  </div>
);

const NewsContent = ({ item, content }) => (
  <div className="news-content">
    {item ? (
      <>
        <h2>{item.title}</h2>
        <p className="content-meta">{formatDate(item.fetchedAt)}</p>
        <a href={item.sourceUrl} target="_blank" rel="noreferrer">
          원문 보기
        </a>
        <pre>{content}</pre>
      </>
    ) : (
      <div className="empty">뉴스를 선택하세요.</div>
    )}
  </div>
);

const ChatPanel = ({ authToken }) => {
  const [message, setMessage] = useState('');
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  const sendMessage = async () => {
    if (!message.trim()) return;
    setLoading(true);
    const userEntry = { role: 'user', text: message };
    setHistory((prev) => [...prev, userEntry]);
    setMessage('');
    try {
      const response = await fetch(`${API_BASE}/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(authToken ? { Authorization: `Bearer ${authToken}` } : {})
        },
        body: JSON.stringify({ message: userEntry.text })
      });
      const data = await response.json();
      setHistory((prev) => [...prev, { role: 'assistant', text: data.response }]);
    } catch (error) {
      setHistory((prev) => [...prev, { role: 'assistant', text: '응답에 실패했습니다.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chat-panel">
      <div className="chat-history">
        {history.map((entry, index) => (
          <div key={`${entry.role}-${index}`} className={`chat-line ${entry.role}`}>
            <strong>{entry.role === 'user' ? '나' : 'GPT'}:</strong> {entry.text}
          </div>
        ))}
        {history.length === 0 && <div className="empty">대화를 시작하세요.</div>}
      </div>
      <div className="chat-input">
        <input
          type="text"
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          placeholder="경제, 정치, 시사 질문을 입력하세요"
        />
        <button type="button" onClick={sendMessage} disabled={loading}>
          {loading ? '전송 중...' : '전송'}
        </button>
      </div>
    </div>
  );
};

const MyPagePanel = ({ authToken, onLogin, onLogout, profile }) => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const handleLogin = async () => {
    setLoading(true);
    await onLogin(email);
    setLoading(false);
  };

  return (
    <div className="mypage-panel">
      <h2>마이페이지</h2>
      <p>ChatGPT Google 계정으로 로그인합니다.</p>
      {profile ? (
        <div className="profile-box">
          <div>이메일: {profile.email}</div>
          <div>구독 플랜: {profile.subscriptionPlan}</div>
          <div>토큰 사용량: {profile.tokenUsed.toLocaleString('ko-KR')}</div>
          <div>잔여 토큰: {(profile.tokenLimit - profile.tokenUsed).toLocaleString('ko-KR')}</div>
          <button type="button" onClick={onLogout} className="secondary">
            로그아웃
          </button>
        </div>
      ) : (
        <div className="login-box">
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Google 이메일 입력"
          />
          <button type="button" onClick={handleLogin} disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </div>
      )}
      <div className="token-hint">
        로그인 세션은 1일 동안 유지됩니다.
        {authToken ? <span> 세션 토큰: {authToken.slice(0, 8)}...</span> : null}
      </div>
    </div>
  );
};

const App = () => {
  const [activeTab, setActiveTab] = useState('BLOOMBERG');
  const [newsItems, setNewsItems] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [content, setContent] = useState('');
  const [authToken, setAuthToken] = useState(() => localStorage.getItem('authToken'));
  const [profile, setProfile] = useState(null);

  const currentTab = useMemo(() => tabs.find((tab) => tab.key === activeTab), [activeTab]);

  const loadProfile = async (token) => {
    if (!token) {
      setProfile(null);
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/auth/me`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!response.ok) {
        setProfile(null);
        return;
      }
      const data = await response.json();
      setProfile(data);
    } catch (error) {
      setProfile(null);
    }
  };

  useEffect(() => {
    loadProfile(authToken);
  }, [authToken]);

  useEffect(() => {
    if (activeTab === 'CHAT' || activeTab === 'MYPAGE') return;
    const load = async () => {
      const response = await fetch(`${API_BASE}/news?source=${activeTab}`);
      const data = await response.json();
      setNewsItems(data);
      if (data.length > 0) {
        setSelectedItem(data[0]);
      } else {
        setSelectedItem(null);
        setContent('');
      }
    };
    load();
  }, [activeTab]);

  useEffect(() => {
    if (!selectedItem) return;
    const loadContent = async () => {
      const response = await fetch(`${API_BASE}/news/${selectedItem.id}/content`);
      const text = await response.text();
      setContent(text);
    };
    loadContent();
  }, [selectedItem]);

  const handleLogin = async (email) => {
    if (!email) return;
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    const data = await response.json();
    localStorage.setItem('authToken', data.sessionToken);
    setAuthToken(data.sessionToken);
  };

  const handleLogout = async () => {
    if (authToken) {
      await fetch(`${API_BASE}/auth/logout`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${authToken}` }
      });
    }
    localStorage.removeItem('authToken');
    setAuthToken(null);
    setProfile(null);
  };

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>SNS GPT 뉴스 요약</h1>
          <p>Bloomberg · Investing.com · GPT10 · ChatGPT</p>
        </div>
        <div className="plan-summary">
          <div className="plan-label">ChatGPT 플랜</div>
          <div className="plan-value">{profile?.subscriptionPlan ?? '로그인 필요'}</div>
          <div className="plan-metrics">
            <span>사용 토큰: {profile ? profile.tokenUsed.toLocaleString('ko-KR') : '-'}</span>
            <span>잔여 토큰: {profile ? (profile.tokenLimit - profile.tokenUsed).toLocaleString('ko-KR') : '-'}</span>
          </div>
        </div>
      </header>
      <main className="content">
        {activeTab === 'CHAT' ? (
          <ChatPanel authToken={authToken} />
        ) : activeTab === 'MYPAGE' ? (
          <MyPagePanel
            authToken={authToken}
            onLogin={handleLogin}
            onLogout={handleLogout}
            profile={profile}
          />
        ) : (
          <div className="news-layout">
            <NewsList items={newsItems} onSelect={setSelectedItem} activeId={selectedItem?.id} />
            <NewsContent item={selectedItem} content={content} />
          </div>
        )}
      </main>
      <nav className="tabs">
        <div className="tabs-left">
          {tabs
            .filter((tab) => tab.key !== 'MYPAGE')
            .map((tab) => (
              <button
                key={tab.key}
                type="button"
                className={tab.key === activeTab ? 'tab active' : 'tab'}
                onClick={() => setActiveTab(tab.key)}
              >
                {tab.label}
              </button>
            ))}
        </div>
        <button
          type="button"
          className={activeTab === 'MYPAGE' ? 'tab active mypage' : 'tab mypage'}
          onClick={() => setActiveTab('MYPAGE')}
        >
          마이페이지
        </button>
      </nav>
      <footer className="footer">현재 탭: {currentTab?.label}</footer>
    </div>
  );
};

export default App;
