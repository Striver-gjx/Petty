import { useEffect, useState } from 'react';
import { UserCheck, Star, Search, Plus, Pencil } from 'lucide-react';
import { sitterApi } from '../api';
import Modal from '../components/Modal';

interface Sitter {
  id: number;
  name: string;
  phone: string;
  serviceArea: string;
  rating: number;
  totalOrders: number;
  status: string;
  bio: string;
  experienceYears: number;
  basePrice: number;
}

interface SitterForm {
  name: string;
  phone: string;
  serviceArea: string;
  bio: string;
  experienceYears: number;
  basePrice: number;
}

const emptySitterForm: SitterForm = {
  name: '', phone: '', serviceArea: '', bio: '', experienceYears: 0, basePrice: 0,
};

const statusMap: Record<string, { label: string; color: string }> = {
  ACTIVE: { label: '在线', color: 'bg-green-100 text-green-700' },
  PENDING_REVIEW: { label: '待审核', color: 'bg-yellow-100 text-yellow-700' },
  SUSPENDED: { label: '暂停', color: 'bg-red-100 text-red-700' },
  OFFLINE: { label: '离线', color: 'bg-gray-100 text-gray-700' },
  BANNED: { label: '封禁', color: 'bg-red-200 text-red-800' },
};

const INPUT = 'w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500';

export default function SittersPage() {
  const [sitters, setSitters] = useState<Sitter[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<SitterForm>(emptySitterForm);
  const [saving, setSaving] = useState(false);

  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectId, setRejectId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  useEffect(() => {
    sitterApi.list()
      .then((res) => setSitters((res as { data: Sitter[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function refreshList() {
    sitterApi.list()
      .then((res) => setSitters((res as { data: Sitter[] }).data))
      .catch(() => {});
  }

  function openCreate() {
    setForm(emptySitterForm);
    setEditingId(null);
    setFormOpen(true);
  }

  function openEdit(s: Sitter) {
    setForm({
      name: s.name,
      phone: s.phone,
      serviceArea: s.serviceArea,
      bio: s.bio,
      experienceYears: s.experienceYears,
      basePrice: s.basePrice,
    });
    setEditingId(s.id);
    setFormOpen(true);
  }

  async function handleSubmit() {
    setSaving(true);
    try {
      const payload: Record<string, unknown> = { ...form };
      if (editingId !== null) {
        await sitterApi.update(editingId, payload);
      } else {
        await sitterApi.create(payload);
      }
      setFormOpen(false);
      refreshList();
    } catch (err) {
      alert('操作失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
    setSaving(false);
  }

  async function handleApprove(id: number) {
    try {
      await sitterApi.approve(id);
      refreshList();
    } catch (err) {
      alert('审核失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
  }

  async function handleRejectSubmit() {
    if (rejectId === null) return;
    setSaving(true);
    try {
      await sitterApi.reject(rejectId, rejectReason || undefined);
      setRejectOpen(false);
      setRejectReason('');
      refreshList();
    } catch (err) {
      alert('拒绝失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
    setSaving(false);
  }

  async function handleSuspend(id: number) {
    try {
      await sitterApi.suspend(id);
      refreshList();
    } catch (err) {
      alert('暂停失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
  }

  async function handleActivate(id: number) {
    try {
      await sitterApi.activate(id);
      refreshList();
    } catch (err) {
      alert('激活失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
  }

  const filtered = sitters.filter((s) => {
    if (!search) return true;
    const q = search.toLowerCase();
    return s.name.toLowerCase().includes(q)
      || s.phone.includes(q)
      || (s.serviceArea ?? '').toLowerCase().includes(q);
  });

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <UserCheck className="w-6 h-6" /> 喂养师管理
        </h2>
        <button
          onClick={openCreate}
          className="flex items-center gap-1.5 px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white rounded-lg text-sm font-medium transition-colors"
        >
          <Plus className="w-4 h-4" /> 添加
        </button>
      </div>

      <div className="relative max-w-sm mb-4">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          placeholder="搜索姓名、手机号或服务区域..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500"
        />
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left font-medium text-gray-500">姓名</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">手机号</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">服务区域</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">评分</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">服务次数</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">基础报价</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">状态</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filtered.map((s) => {
              const st = statusMap[s.status] ?? { label: s.status, color: 'bg-gray-100 text-gray-700' };
              return (
                <tr key={s.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-800">{s.name}</td>
                  <td className="px-6 py-4 text-gray-600">{s.phone}</td>
                  <td className="px-6 py-4 text-gray-600">{s.serviceArea || '-'}</td>
                  <td className="px-6 py-4">
                    <span className="flex items-center gap-1">
                      <Star className="w-4 h-4 text-yellow-400 fill-yellow-400" />
                      {s.rating}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{s.totalOrders}</td>
                  <td className="px-6 py-4 text-gray-600">¥{s.basePrice}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${st.color}`}>
                      {st.label}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => openEdit(s)}
                        className="p-1 text-gray-400 hover:text-orange-500 transition-colors"
                        title="编辑"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      {s.status === 'PENDING_REVIEW' && (
                        <>
                          <button
                            onClick={() => handleApprove(s.id)}
                            className="px-2 py-1 text-xs bg-green-50 text-green-600 rounded-md hover:bg-green-100 font-medium transition-colors"
                          >
                            通过
                          </button>
                          <button
                            onClick={() => { setRejectId(s.id); setRejectReason(''); setRejectOpen(true); }}
                            className="px-2 py-1 text-xs bg-red-50 text-red-600 rounded-md hover:bg-red-100 font-medium transition-colors"
                          >
                            拒绝
                          </button>
                        </>
                      )}
                      {s.status === 'ACTIVE' && (
                        <button
                          onClick={() => handleSuspend(s.id)}
                          className="px-2 py-1 text-xs bg-yellow-50 text-yellow-700 rounded-md hover:bg-yellow-100 font-medium transition-colors"
                        >
                          暂停
                        </button>
                      )}
                      {s.status === 'SUSPENDED' && (
                        <button
                          onClick={() => handleActivate(s.id)}
                          className="px-2 py-1 text-xs bg-green-50 text-green-600 rounded-md hover:bg-green-100 font-medium transition-colors"
                        >
                          激活
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {filtered.length === 0 && (
          <div className="text-center py-12 text-gray-400">暂无喂养师数据</div>
        )}
      </div>

      {/* Add/Edit Form Modal */}
      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editingId !== null ? '编辑喂养师' : '添加喂养师'}>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">姓名</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">手机号</label>
            <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">服务区域</label>
            <input value={form.serviceArea} onChange={(e) => setForm({ ...form, serviceArea: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">简介</label>
            <textarea value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} rows={3} className={INPUT} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">经验年数</label>
              <input
                type="number" min={0}
                value={form.experienceYears}
                onChange={(e) => setForm({ ...form, experienceYears: Number(e.target.value) || 0 })}
                className={INPUT}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">基础报价 (¥)</label>
              <input
                type="number" min={0} step={0.01}
                value={form.basePrice}
                onChange={(e) => setForm({ ...form, basePrice: Number(e.target.value) || 0 })}
                className={INPUT}
              />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setFormOpen(false)} className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
              取消
            </button>
            <button onClick={handleSubmit} disabled={saving} className="px-4 py-2 text-sm text-white bg-orange-500 hover:bg-orange-600 disabled:opacity-50 rounded-lg font-medium transition-colors">
              {saving ? '保存中...' : '保存'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Reject Reason Modal */}
      <Modal open={rejectOpen} onClose={() => setRejectOpen(false)} title="拒绝审核">
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">拒绝原因（可选）</label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              rows={3}
              placeholder="请输入拒绝原因..."
              className={INPUT}
            />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setRejectOpen(false)} className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
              取消
            </button>
            <button onClick={handleRejectSubmit} disabled={saving} className="px-4 py-2 text-sm text-white bg-red-500 hover:bg-red-600 disabled:opacity-50 rounded-lg font-medium transition-colors">
              {saving ? '提交中...' : '确认拒绝'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
