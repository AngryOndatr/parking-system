import { useState, useRef, useEffect } from 'react'
import { ChevronDown, X } from 'lucide-react'

export interface ComboboxOption {
  value: string | number
  label: string
}

interface ComboboxProps {
  options: ComboboxOption[]
  value: string | number | undefined
  onChange: (value: string | number | undefined) => void
  placeholder?: string
  isLoading?: boolean
  disabled?: boolean
  className?: string
}

export function Combobox({
  options,
  value,
  onChange,
  placeholder = 'Select...',
  isLoading = false,
  disabled = false,
  className = '',
}: ComboboxProps) {
  const [open, setOpen] = useState(false)
  const [inputValue, setInputValue] = useState('')
  const containerRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const selectedOption = options.find(opt => opt.value === value)
  const filteredOptions = options.filter(opt =>
    opt.label.toLowerCase().includes(inputValue.toLowerCase())
  )

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
        setInputValue('')
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <div ref={containerRef} className={`relative w-full ${className}`}>
      <button
        type="button"
        onClick={() => {
          setOpen(!open)
          if (!open) inputRef.current?.focus()
        }}
        disabled={disabled}
        className="w-full h-8 px-3 py-1 text-sm border rounded bg-white text-left flex items-center justify-between hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span className={selectedOption ? 'text-slate-900' : 'text-slate-400'}>
          {selectedOption?.label || placeholder}
        </span>
        <ChevronDown size={16} className="text-slate-400 flex-shrink-0" />
      </button>

      {open && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white border rounded shadow-lg z-50">
          <input
            ref={inputRef}
            type="text"
            placeholder={placeholder}
            value={inputValue}
            onChange={e => setInputValue(e.target.value)}
            className="w-full px-3 py-1.5 text-sm border-b outline-none"
            autoFocus
          />
          <div className="max-h-48 overflow-y-auto">
            {isLoading && (
              <div className="px-3 py-2 text-sm text-slate-500 text-center">Loading...</div>
            )}
            {!isLoading && filteredOptions.length === 0 && (
              <div className="px-3 py-2 text-sm text-slate-500 text-center">No results</div>
            )}
            {!isLoading && filteredOptions.map(option => (
              <button
                key={option.value}
                type="button"
                onClick={() => {
                  onChange(option.value)
                  setOpen(false)
                  setInputValue('')
                }}
                className={`w-full px-3 py-1.5 text-sm text-left hover:bg-blue-50 ${
                  value === option.value ? 'bg-blue-100 text-blue-900' : ''
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>
      )}

      {value && selectedOption && (
        <button
          type="button"
          onClick={() => {
            onChange(undefined)
            setInputValue('')
          }}
          className="absolute right-8 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0"
        >
          <X size={16} />
        </button>
      )}
    </div>
  )
}
