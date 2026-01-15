import React, { useEffect, useMemo, useState } from 'react';

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api';

const tabs = [
  { key: 'BLOOMBERG', label: 'Bloomberg' },
  { key: 'INVESTING_USA', label: 'Investing.com USA' },
  { key: 'INVESTING_KOREA', label: 'Investing.com Korea' },
  { key: 'INVESTING_JAPAN', label: 'Investing.com Japan' },
  { key: 'INVESTING_CHINA', label: 'Investing.com China' },
  { key: 'GPT10', label: 'GPT10' },
  { key: 'CHAT', label: 'Chat with GPT' }
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

const ChatPanel = () => {
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
        headers: { 'Content-Type': 'application/json' },
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

const App = () => {
  const [activeTab, setActiveTab] = useState('BLOOMBERG');
  const [newsItems, setNewsItems] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [content, setContent] = useState('');

  const currentTab = useMemo(() => tabs.find((tab) => tab.key === activeTab), [activeTab]);

  useEffect(() => {
    if (activeTab === 'CHAT') return;
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

  return (
    <div className="app">
      <header className="header">
        <h1>SNS GPT 뉴스 요약</h1>
        <p>Bloomberg · Investing.com (USA/Korea/Japan/China) · GPT10 · ChatGPT</p>
      </header>
      <main className="content">
        {activeTab === 'CHAT' ? (
          <ChatPanel />
        ) : (
          <div className="news-layout">
            <NewsList items={newsItems} onSelect={setSelectedItem} activeId={selectedItem?.id} />
            <NewsContent item={selectedItem} content={content} />
          </div>
        )}
      </main>
      <nav className="tabs">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={tab.key === activeTab ? 'tab active' : 'tab'}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </nav>
      <footer className="footer">
        현재 탭: {currentTab?.label}
      </footer>
    </div>
  );
};

export default App;
