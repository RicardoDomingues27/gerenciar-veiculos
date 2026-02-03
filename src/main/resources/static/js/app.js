const API = '/veiculos';

/* =====================
   STATE
===================== */
let userRole = null;

/* =====================
   HELPERS
===================== */
const $ = (id) => document.getElementById(id);

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem('token')}`
    };
}

function showToast(message, type = 'success') {
    const toast = $('toast');
    toast.innerText = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 4000);
}

function isAdmin() {
    return userRole === 'ROLE_ADMIN';
}

/* =====================
   INIT
===================== */
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (token && role) {
        userRole = role;
        loginSuccess();
    }
});

/* =====================
   LOGIN / LOGOUT
===================== */
async function login() {
    try {
        const res = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: username.value,
                password: password.value
            })
        });

        if (!res.ok) {
            showToast('Usuário ou senha inválidos', 'error');
            return;
        }

        const data = await res.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('role', data.role);
        userRole = data.role;

        showToast('Login realizado com sucesso');
        loginSuccess();
    } catch {
        showToast('Erro ao realizar login', 'error');
    }
}

function loginSuccess() {
    $('login').classList.add('hidden');
    $('app').classList.remove('hidden');

    $('card-cadastrar').classList.toggle('hidden', !isAdmin());
    loadVeiculos();
}

function logout() {
    localStorage.clear();
    showToast('Logout realizado com sucesso', 'error');
    setTimeout(() => location.reload(), 800);
}

/* =====================
   VEÍCULOS
===================== */
async function loadVeiculos() {
    try {
        const res = await fetch(API, { headers: authHeaders() });
        if (!res.ok) throw new Error();

        const data = await res.json();
        renderVeiculos(data.content ?? data ?? []);
    } catch {
        showToast('Erro ao carregar veículos', 'error');
    }
}

function renderVeiculos(veiculos = []) {
    const ul = $('veiculos');
    const empty = $('emptyMessage');
    ul.innerHTML = '';

    if (veiculos.length === 0) {
        empty.style.display = 'block';
        return;
    }

    empty.style.display = 'none';

    veiculos.forEach(v => {
        const li = document.createElement('li');

        li.innerHTML = `
            <span>
                <strong>${v.marca} ${v.modelo}</strong><br>
                Ano: ${v.ano} | Cor: ${v.cor}<br>
                Placa: ${v.placa}<br>
                USD ${(v.precoDolar ?? 0).toFixed(2)}
            </span>
        `;

        if (isAdmin()) {
            li.innerHTML += `
                <div class="actions">
                    <button class="edit" onclick='editVeiculo(${JSON.stringify(v)})'>Editar</button>
                    <button class="delete" onclick='deleteVeiculo(${v.id})'>Excluir</button>
                </div>
            `;
        }

        ul.appendChild(li);
    });
}

/* =====================
   CRUD (ADMIN)
===================== */
async function saveVeiculo() {
    if (!isAdmin()) return;

    const id = veiculoId.value;
    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API}/${id}` : API;

    const veiculo = {
        marca: marca.value,
        modelo: modelo.value,
        ano: Number(ano.value),
        cor: cor.value,
        placa: placa.value,
        precoDolar: Number(precoDolar.value)
    };

    try {
        const res = await fetch(url, {
            method,
            headers: authHeaders(),
            body: JSON.stringify(veiculo)
        });

        if (!res.ok) {
            const msg = await res.text();
            showToast(
                msg.toLowerCase().includes('placa')
                    ? 'Placa já cadastrada'
                    : 'Veículo já cadastrado',
                'error'
            );
            return;
        }

        showToast('Veículo salvo com sucesso');
        clearForm();
        loadVeiculos();
    } catch {
        showToast('Erro ao salvar veículo', 'error');
    }
}

function editVeiculo(v) {
    if (!isAdmin()) return;

    veiculoId.value = v.id;
    marca.value = v.marca;
    modelo.value = v.modelo;
    ano.value = v.ano;
    cor.value = v.cor;
    placa.value = v.placa;
    precoDolar.value = v.precoDolar ?? '';
}

async function deleteVeiculo(id) {
    if (!isAdmin() || !confirm('Deseja excluir este veículo?')) return;

    try {
        const res = await fetch(`${API}/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });

        if (!res.ok) throw new Error();

        showToast('Veículo excluído com sucesso');
        loadVeiculos();
    } catch {
        showToast('Erro ao excluir veículo', 'error');
    }
}

/* =====================
   FORM
===================== */
function clearForm() {
    veiculoId.value = '';
    marca.value = '';
    modelo.value = '';
    ano.value = '';
    cor.value = '';
    placa.value = '';
    precoDolar.value = '';
}

/* =====================
   FILTRO
===================== */
async function filtrarVeiculos() {
    const params = new URLSearchParams();

    if (filtroMarca.value) params.append('marca', filtroMarca.value);
    if (filtroAno.value) params.append('ano', filtroAno.value);
    if (filtroCor.value) params.append('cor', filtroCor.value);
    if (filtroMinPreco.value) params.append('minPreco', filtroMinPreco.value);
    if (filtroMaxPreco.value) params.append('maxPreco', filtroMaxPreco.value);

    try {
        const res = await fetch(`/veiculos/busca?${params}`, {
            headers: authHeaders()
        });

        if (!res.ok) throw new Error();

        const data = await res.json();
        renderVeiculos(data);
    } catch {
        showToast('Erro ao aplicar filtros', 'error');
    }
}
