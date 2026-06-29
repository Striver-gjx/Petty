import { useEffect, useState } from 'react';
import { Users, Search, Plus, Pencil, Trash2 } from 'lucide-react';
import { ownerApi } from '../api';
import Modal from '../components/Modal';

interface Owner {
  id: number;
  nickname: string;
  phone: string;
  address: string;
  memberLevel: string;
  totalOrders: number;
  totalSpent: number;
}

interface OwnerForm {
  nickname: string;
  phone: string;
  address: string;
  memberLevel: string;
}

const emptyForm: OwnerForm = { nickname: '', phone: '', address: '', memberLevel: 'NORMAL' };

const levelMap: Record<string, { label: string; color: string }> = {
  NORMAL: { label: '普通', color: 'bg-gray-100 text-gray-700' },
  VIP: { label: 'VIP', color: 'bg-purple-100 text-purple-700' },
  SVIP: { label: 'SVIP', color: 'bg-orange-100 text-orange-700' },
};

const INPUT = 'w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500';

export default function OwnersPage() {
  const [owners, setOwners] = useState<Owner[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<OwnerForm>(emptyForm);
  const [saving, setSaving] = useState(false);

  const [deleteId, setDeleteId] = useState<number | null>(null);

  useEffect(() => {
    ownerApi.list()
      .then((res) => setOwners((res as { data: Owner[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function refreshList() {
    ownerApi.list()
      .then((res) => setOwners((res as { data: Owner[] }).data))
      .catch(() => {});
  }

  function openCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setFormOpen(true);
  }

  function openEdit(o: Owner) {
    setForm({
      nickname: o.nickname,
      phone: o.phone,
      address: o.address,
      memberLevel: o.memberLevel,
    });
    setEditingId(o.id);
    setFormOpen(true);
  }

  async function handleSubmit() {
    setSaving(true);
    try {
      const payload: Record<string, unknown> = { ...form };
      if (editingId !== null) {
        await ownerApi.update(editingId, payload);
      } else {
        await ownerApi.create(payload);
      }
      setFormOpen(false);
      refreshList();
    } catch (err) {
      alert('操作失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
    setSaving(false);
  }

  async function handleDelete() {
    if (deleteId === null) return;
    try {
      await ownerApi.delete(deleteId);
      setDeleteId(null);
      refreshList();
    } catch (err) {
      alert('删除失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
  }

  const filtered = owners.filter((o) => {
    if (!search) return true;
    const q = search.toLowerCase();
    return o.nickname.toLowerCase().includes(q)
      || o.phone.includes(q)
      || (o.address ?? '').toLowerCase().includes(q);
  });

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Users className="w-6 h-6" /> 宠物主人
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
          placeholder="搜索昵称、手机号或地址..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500"
        />
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left font-medium text-gray-500">昵称</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">手机号</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">地址</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">等级</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">订单数</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">累计消费</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filtered.map((o) => {
              const lv = levelMap[o.memberLevel] ?? { label: o.memberLevel, color: 'bg-gray-100 text-gray-700' };
              return (
                <tr key={o.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-800">{o.nickname}</td>
                  <td className="px-6 py-4 text-gray-600">{o.phone}</td>
                  <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{o.address || '-'}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${lv.color}`}>
                      {lv.label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{o.totalOrders}</td>
                  <td className="px-6 py-4 text-gray-600">¥{o.totalSpent}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <button onClick={() => openEdit(o)} className="p-1 text-gray-400 hover:text-orange-500 transition-colors" title="编辑">
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button onClick={() => setDeleteId(o.id)} className="p-1 text-gray-400 hover:text-red-500 transition-colors" title="删除">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {filtered.length === 0 && (
          <div className="text-center py-12 text-gray-400">暂无宠物主人数据</div>
        )}
      </div>

      {/* Add/Edit Form Modal */}
      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editingId !== null ? '编辑宠物主人' : '添加宠物主人'}>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">昵称</label>
            <input value={form.nickname} onChange={(e) => setForm({ ...form, nickname: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">手机号</label>
            <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">地址</label>
            <input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} className={INPUT} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">会员等级</label>
            <select value={form.memberLevel} onChange={(e) => setForm({ ...form, memberLevel: e.target.value })} className={INPUT}>
              <option value="NORMAL">普通</option>
              <option value="VIP">VIP</option>
              <option value="SVIP">SVIP</option>
            </select>
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

      {/* Delete Confirmation Modal */}
      <Modal open={deleteId !== null} onClose={() => setDeleteId(null)} title="确认删除">
        <div>
          <p className="text-gray-600 mb-6">确定要删除该宠物主人吗？此操作不可撤销。</p>
          <div className="flex justify-end gap-3">
            <button onClick={() => setDeleteId(null)} className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
              取消
            </button>
            <button onClick={handleDelete} className="px-4 py-2 text-sm text-white bg-red-500 hover:bg-red-600 rounded-lg font-medium transition-colors">
              确认删除
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
