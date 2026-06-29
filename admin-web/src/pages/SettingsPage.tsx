import { useEffect, useState } from 'react';
import { Settings, Plus, Pencil, Trash2 } from 'lucide-react';
import { serviceTypeApi } from '../api';
import Modal from '../components/Modal';

interface ServiceType {
  id: number;
  name: string;
  description: string;
  basePrice: number;
  duration: number;
}

interface ServiceTypeForm {
  name: string;
  description: string;
  basePrice: number;
  duration: number;
}

const emptyForm: ServiceTypeForm = { name: '', description: '', basePrice: 0, duration: 60 };

const INPUT = 'w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500';

export default function SettingsPage() {
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<ServiceTypeForm>(emptyForm);
  const [saving, setSaving] = useState(false);

  const [deleteId, setDeleteId] = useState<number | null>(null);

  useEffect(() => {
    serviceTypeApi.list()
      .then((res) => setServiceTypes((res as { data: ServiceType[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function refreshList() {
    serviceTypeApi.list()
      .then((res) => setServiceTypes((res as { data: ServiceType[] }).data))
      .catch(() => {});
  }

  function openCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setFormOpen(true);
  }

  function openEdit(st: ServiceType) {
    setForm({
      name: st.name,
      description: st.description,
      basePrice: st.basePrice,
      duration: st.duration,
    });
    setEditingId(st.id);
    setFormOpen(true);
  }

  async function handleSubmit() {
    setSaving(true);
    try {
      const payload: Record<string, unknown> = { ...form };
      if (editingId !== null) {
        await serviceTypeApi.update(editingId, payload);
      } else {
        await serviceTypeApi.create(payload);
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
      await serviceTypeApi.delete(deleteId);
      setDeleteId(null);
      refreshList();
    } catch (err) {
      alert('删除失败：' + (err instanceof Error ? err.message : '未知错误'));
    }
  }

  return (
    <div>
      <div className="flex items-center gap-2 mb-6">
        <Settings className="w-6 h-6" />
        <h2 className="text-xl font-bold text-gray-800">系统设置</h2>
      </div>

      {/* Commission Rate */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 mb-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-4">佣金设置</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-500">平台佣金比例</p>
            <p className="text-2xl font-bold text-orange-500 mt-1">20%</p>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-500">喂养师分成比例</p>
            <p className="text-2xl font-bold text-green-600 mt-1">80%</p>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-500">最低提现金额</p>
            <p className="text-2xl font-bold text-gray-800 mt-1">¥50</p>
          </div>
        </div>
        <p className="text-xs text-gray-400 mt-3">佣金比例调整请联系系统管理员</p>
      </div>

      {/* System Info */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 mb-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-4">系统信息</h3>
        <div className="grid grid-cols-2 gap-4 text-sm text-gray-600">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="font-medium text-gray-800">后端状态</p>
            <p className="text-green-600 mt-1">运行中 (localhost:18080)</p>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="font-medium text-gray-800">数据库</p>
            <p className="text-green-600 mt-1">H2 内存数据库</p>
          </div>
        </div>
      </div>

      {/* Service Types */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-800">服务类型管理</h3>
          <button
            onClick={openCreate}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-orange-500 hover:bg-orange-600 text-white rounded-lg text-sm font-medium transition-colors"
          >
            <Plus className="w-4 h-4" /> 添加
          </button>
        </div>

        {loading ? (
          <p className="text-gray-400 text-sm">加载中...</p>
        ) : serviceTypes.length === 0 ? (
          <p className="text-gray-400 text-sm text-center py-8">暂无服务类型</p>
        ) : (
          <div className="space-y-3">
            {serviceTypes.map((st) => (
              <div key={st.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div className="flex-1">
                  <div className="flex items-center gap-3">
                    <h4 className="font-medium text-gray-800">{st.name}</h4>
                    <span className="text-xs bg-orange-100 text-orange-600 px-2 py-0.5 rounded-full">
                      ¥{st.basePrice}/次
                    </span>
                    {st.duration > 0 && (
                      <span className="text-xs text-gray-400">{st.duration} 分钟</span>
                    )}
                  </div>
                  {st.description && (
                    <p className="text-sm text-gray-500 mt-1">{st.description}</p>
                  )}
                </div>
                <div className="flex items-center gap-1 ml-4">
                  <button onClick={() => openEdit(st)} className="p-1.5 text-gray-400 hover:text-orange-500 transition-colors" title="编辑">
                    <Pencil className="w-4 h-4" />
                  </button>
                  <button onClick={() => setDeleteId(st.id)} className="p-1.5 text-gray-400 hover:text-red-500 transition-colors" title="删除">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add/Edit Service Type Modal */}
      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editingId !== null ? '编辑服务类型' : '添加服务类型'}>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">名称</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className={INPUT} placeholder="如：日常喂养" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">描述</label>
            <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows={3} className={INPUT} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">基础价格 (¥)</label>
              <input
                type="number" min={0} step={0.01}
                value={form.basePrice || ''}
                onChange={(e) => setForm({ ...form, basePrice: Number(e.target.value) || 0 })}
                className={INPUT}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">时长 (分钟)</label>
              <input
                type="number" min={0}
                value={form.duration || ''}
                onChange={(e) => setForm({ ...form, duration: Number(e.target.value) || 0 })}
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

      {/* Delete Confirmation Modal */}
      <Modal open={deleteId !== null} onClose={() => setDeleteId(null)} title="确认删除">
        <div>
          <p className="text-gray-600 mb-6">确定要删除该服务类型吗？此操作不可撤销。</p>
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
